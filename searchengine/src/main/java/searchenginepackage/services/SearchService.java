package searchenginepackage.services;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchenginepackage.entities.*;
import searchenginepackage.model.SingleResult;
import searchenginepackage.model.QueryResult;
import searchenginepackage.repositories.IndexRepository;
import searchenginepackage.repositories.LemmaRepository;
import searchenginepackage.repositories.PageRepository;
import searchenginepackage.repositories.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private PageRepository pageRepo;
    @Autowired
    private SiteRepository siteRepo;
    @Autowired
    private LemmaRepository lemmaRepo;
    @Autowired
    private IndexRepository indexRepo;
    private final MorphologyService morphologyService = new MorphologyService();
    private final ConnectionService connectionService = new ConnectionService();
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    public QueryResult searchAllSites(String query, String site, int offset, int limit) {
        QueryResult queryResult = new QueryResult();
        List<SingleResult> resultList = new ArrayList<>();
        try {
            if (site != null) {
                SiteEntity entity = siteRepo.getReferenceById(siteRepo.findIdByUrl(site));
                List<SingleResult> results = searchSite(query, entity, limit);
                resultList.addAll(results);
            } else {
                log.info("Searching across all sites");
                for (SiteEntity entity : siteRepo.findAll()) {
                    log.info("Searched through site: "+ entity.getUrl());
                    resultList.addAll(searchSite(query, entity, limit));
                }
            }
            log.info("Results found: " + resultList.size());
            if (!resultList.isEmpty()) {
                resultList.sort(Comparator.comparing(SingleResult::getRelevance).reversed());
                queryResult.setData(resultList);
            }
            queryResult.setCount(queryResult.getData().size());
            queryResult.setResult(true);
            log.info("Result ready: " + queryResult.toString());
        } catch (Exception e) {
            log.info("Search disrupted by exception: " + e.getLocalizedMessage());
            queryResult.setResult(false);
        }
        return queryResult;
    }
    private List<SingleResult> searchSite(String query, SiteEntity site, int limit) {
        log.info("query: " + query);
        log.info("site: " + site);
        List<SingleResult> responses = new ArrayList<>();
        Integer siteId = site.getId();
        List<String> queryWords = new ArrayList<>(morphologyService.decomposeTextToLemmasWithRank(query).keySet());
        log.info("Decomposed query into words/lemmas: {}", queryWords);
        List<LemmaEntity> queryLemmas = fetchRelevantLemmas(queryWords, siteId);
        for (LemmaEntity entity : queryLemmas) {
            log.info("found lemma entity: " + entity.toString());
        }
        if (queryLemmas.isEmpty()) {
            log.info("No relevant lemmas found for query: '{}' on site: {}", query, site.getUrl());
            return responses;
        }
        List<PageEntity> pageList = fetchRelevantPages(queryLemmas);
        for (PageEntity page : pageList) {
            log.info("check page: ");
            if (page.getContent() != null) {
                log.info("page content is not null");
            }
            if (page.getPath() != null) {
                log.info("page path: " + page.getPath());
            }
        }
        for (PageEntity entity : pageList) {
            //log.info("Processing page with path: {}", entity.getPath());
            String html = entity.getContent();
            log.info("page content length: " + html.length());
            List<String> snippets = generateSnippets(queryWords, html);
            //log.info("Generated {} snippets for page: {}", snippets.size(), entity.getPath());
            float relevance = calculateRelevance(entity, queryLemmas);
            log.info("Calculated relevance for page '{}': {}", entity.getPath(), relevance);
            for (String snippet : snippets) {
                String siteUrl = site.getUrl();
                String entityPath = entity.getPath();
                String title = connectionService.getTitle(entity.getContent());
                log.info("Data before adding the result: \n" + "site url: " + site.getUrl() + "\nentity path: " + entity.getPath() + "\ntitle: " + title + "\nsnippet: " + snippet + "\nrelevance: " + relevance);
                responses.add(new SingleResult(siteUrl, entityPath, title, snippet, relevance));
            }
        }
        return responses;
    }
    private List<LemmaEntity> fetchRelevantLemmas(List<String> queryWords, Integer siteId) {
        List<LemmaEntity> relevantLemmas = queryWords.stream()
                .map(word -> lemmaRepo.findByLemmaAndSiteId(word, siteId))
                .filter(Objects::nonNull)
                .filter(lemma -> lemma.getFrequency() < lemmaRepo.count() / 2)
                .sorted(Comparator.comparing(LemmaEntity::getFrequency).reversed())
                .collect(Collectors.toList());
        return relevantLemmas;
    }
    private List<PageEntity> fetchRelevantPages(List<LemmaEntity> queryLemmas) {
        List<PageEntity> pages = queryLemmas.stream()
                .flatMap(lemma -> indexRepo.findAllPagesByLemma(lemma).stream())
                .collect(Collectors.toList());
        return pages;
    }
    private float calculateRelevance(PageEntity entity, List<LemmaEntity> queryLemmas) {
        String htmlContent = entity.getContent();
        String title = connectionService.getTitle(htmlContent);
        String body = connectionService.getBody(htmlContent);
        Map<String, Integer> titleLemmas = morphologyService.decomposeTextToLemmasWithRank(title);
        Map<String, Integer> bodyLemmas = morphologyService.decomposeTextToLemmasWithRank(body);
        float titleWeight = 2.0f;
        float bodyWeight = 1.0f;
        return queryLemmas.stream()
                .map(lemma -> {
                    IndexEntity indexEntity = indexRepo.findByPageAndLemma(entity, lemma);
                    if (indexEntity == null) {
                        return 0f;
                    }
                    String lemmaName = lemma.getLemma();
                    float rank = indexEntity.getRankScore();
                    if (titleLemmas.containsKey(lemmaName)) {
                        rank *= titleWeight;
                    } else if (bodyLemmas.containsKey(lemmaName)) {
                        rank *= bodyWeight;
                    }
                    return rank;
                })
                .reduce(0f, Float::sum);
    }

    private List<String> generateSnippets(List<String> queryWords, String html) {
        Document document = Jsoup.parse(html);
        Set<String> snippets = new LinkedHashSet<>();
        document.body().select("*").forEach(element -> {
                if (new HashSet<>(morphologyService.lemmatizeElementContent(element)).containsAll(queryWords)) {
                    String text = element.text();
                    String highlightedSnippet = highlightQueryWordsAndTrim(text, queryWords);
                    if (!highlightedSnippet.isEmpty() && highlightedSnippet.trim().length() > 2) {
                        snippets.add(highlightedSnippet);
                    }
                }
        });
        return new ArrayList<>(snippets);
    }
    private String highlightQueryWordsAndTrim(String text, List<String> queryWords) {
        if (text == null || text.isBlank() || queryWords.isEmpty()) {
            return "";
        }
        int wordsBefore = 2;
        int wordsAfter = 20;
        int totalLimit = 33;
        log.info("Trimming snippet: {}", text);
        String[] words = text.split("\\s+");
        StringBuilder snippetBuilder = new StringBuilder();
        boolean isQueryMatched = false;
        int totalWordsAdded = 0;
        for (int i = 0; i < words.length; i++) {
            String lemmatizedWord = morphologyService.lemmatizeWord(words[i]);
            if (queryWords.contains(lemmatizedWord)) {
                isQueryMatched = true;
                int start = Math.max(0, i - wordsBefore);
                int end = Math.min(words.length, i + wordsAfter + 1);
                for (int j = start; j < end; j++) {
                    if (totalWordsAdded >= totalLimit) {
                        snippetBuilder.append("... ");
                        return snippetBuilder.toString().trim();
                    }
                    if (j == i) {
                        snippetBuilder.append("<b>").append(words[j]).append("</b> ");
                    } else {
                        snippetBuilder.append(words[j]).append(" ");
                    }
                    totalWordsAdded++;
                }
                snippetBuilder.append("... ");
            }
        }
        return isQueryMatched ? snippetBuilder.toString().trim() : "";
    }

}
