package searchenginepackage.services;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchenginepackage.entities.*;
import searchenginepackage.model.QueryResponse;
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



    public QueryResult searchAllSites(String query, String site, int offset, int limit) {
        QueryResult queryResult = new QueryResult();
        List<QueryResponse> responses = new ArrayList<>();
        try {
            if (site != null) {
                SiteEntity entity = siteRepo.getReferenceById(siteRepo.findIdByUrl(site));
                responses.addAll(searchSite(query, entity, limit));
            } else {
                for (SiteEntity entity : siteRepo.findAll()) {
                    responses.addAll(searchSite(query, entity, limit));
                }
            }
            if (!responses.isEmpty()) {
                responses.sort(Comparator.comparing(QueryResponse::getRelevance).reversed());
                queryResult.setData(responses.subList(Math.min(offset, responses.size()), Math.min(offset + limit, responses.size())));
            }
            queryResult.setCount(queryResult.getData().size());
            queryResult.setResult(true);
        } catch (Exception e) {
            queryResult.setResult(false);
            e.printStackTrace();
        }
        return queryResult;
    }
    private List<QueryResponse> searchSite(String query, SiteEntity site, int limit) {
        List<QueryResponse> responses = new ArrayList<>();
        Integer siteId = site.getId();
        List<String> queryWords = new ArrayList<>(morphologyService.decomposeTextToLemmasWithRank(query).keySet());
        List<LemmaEntity> queryLemmas = fetchRelevantLemmas(queryWords, siteId);
        if (queryLemmas.isEmpty()) {
            return responses;
        }
        LemmaEntity startingLemma = queryLemmas.get(0);
        List<PageEntity> pageList = fetchRelevantPages(startingLemma, queryLemmas);
        for (PageEntity entity : pageList) {
            String html = entity.getContent();
            List<String> snippets = generateSnippets(queryWords, html, limit);
            float relevance = calculateRelevance(entity, queryLemmas);
            String title = connectionService.getTitle(entity.getContent());
            for (String snippet : snippets) {
                responses.add(new QueryResponse(site.getUrl(), entity.getPath(), title, snippet, relevance));
            }
        }
        return responses;
    }
    private List<LemmaEntity> fetchRelevantLemmas(List<String> queryWords, Integer siteId) {
        return queryWords.stream()
                .map(word -> lemmaRepo.getReferenceById(lemmaRepo.findIdByLemmaAndSiteId(word, siteId)))
                .filter(lemma -> lemma.getFrequency() < lemmaRepo.count() / 2)
                .sorted(Comparator.comparing(LemmaEntity::getFrequency).reversed())
                .collect(Collectors.toList());
    }

    private List<PageEntity> fetchRelevantPages(LemmaEntity startingLemma, List<LemmaEntity> queryLemmas) {
        List<PageEntity> pageList = pageRepo.findAllBySiteId(indexRepo.findAllPageIdByLemmaId(startingLemma.getSiteId()));
        return pageList.stream()
                .filter(page -> queryLemmas.stream()
                        .allMatch(queryLemma -> indexRepo.findAllLemmaIdByPageId(page.getId()).contains(queryLemma.getId())))
                .collect(Collectors.toList());
    }

    private float calculateRelevance(PageEntity entity, List<LemmaEntity> queryLemmas) {
        return queryLemmas.stream()
                .map(lemma -> indexRepo.findByPageIdAndLemmaId(entity.getId(), lemma.getId()).getRank())
                .reduce(0f, Float::sum);
    }

    private List<String> generateSnippets(List<String> queryWords, String html, int limit) {
        List<Element> elements = extractElementsWithQueryWords(html, queryWords);
        List<String> snippets = new ArrayList<>();

        for (Element element : elements) {
            String text = element.text();
            String highlightedText = highlightQueryWords(text, queryWords);
            snippets.add(trimSnippet(highlightedText, limit));
        }
        return snippets;
    }

    private List<Element> extractElementsWithQueryWords(String html, List<String> queryWords) {
        return queryWords.stream()
                .flatMap(query -> Jsoup.parse(html).getElementsContainingText(query).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    private String highlightQueryWords(String text, List<String> queryWords) {
        for (String query : queryWords) {
            text = text.replaceAll("(?i)\\b" + query + "\\b", "<b>" + query + "</b>");
        }
        return text;
    }

    private String trimSnippet(String text, int limit) {
        if (text.length() <= limit) {
            return text;
        }
        return text.substring(0, limit).trim() + "...";
    }
}



