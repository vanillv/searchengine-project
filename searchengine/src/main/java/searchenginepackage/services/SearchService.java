package searchenginepackage.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import searchenginepackage.model.*;
import searchenginepackage.repositories.IndexRepository;
import searchenginepackage.repositories.LemmaRepository;
import searchenginepackage.repositories.PageRepository;
import searchenginepackage.repositories.SiteRepository;

import java.util.*;

@Service
public class SearchService {
    private PageRepository pageRepo;
    private SiteRepository siteRepo;
    private LemmaRepository lemmaRepo;
    private IndexRepository indexRepo;
    private MorphologyService morphologyService = new MorphologyService();
    private ConnectionService connectionService = new ConnectionService();
//    @Autowired
//    private SearchService init(PageRepository pageRepository, SiteRepository siteRepository,
//                              LemmaRepository lemmaRepository, IndexRepository indexRepository) {
//        pageRepo = pageRepository; siteRepo = siteRepository; lemmaRepo = lemmaRepository;
//        indexRepo = indexRepository;
//        return this;
//    }
    public QueryResult searchAllSites(String query, String site, int offset, int limit) {
        QueryResult queryResult = new QueryResult();
        try {
            List<QueryResponse> responses;
            if(!site.isEmpty()) {
                SiteEntity entity = siteRepo.getReferenceById(siteRepo.findByUrl(site));
                responses = searchSite(query, entity);
                if (!responses.isEmpty()) {
                    queryResult.getData().addAll(searchSite(query, entity));
                }
            } else {
                for (SiteEntity entity : siteRepo.findAll()) {
                    responses = searchSite(query, entity);
                    if (!responses.isEmpty()) {
                        queryResult.getData().addAll(responses);
                    }
                }
            }
            queryResult.setCount(queryResult.getData().size());
            queryResult.setResult(true);
            return queryResult;
        }  catch (Exception e) {
            queryResult.setResult(false);
            return queryResult;
        }


    }
    public List<QueryResponse> searchSite(String query, SiteEntity site) {
        List<QueryResponse> responses = new ArrayList<>();
        Integer siteId = site.getId();
        //List<PageEntity> pageList = pageRepo.findAllBySite(siteRepo.findByUrl(site));
        List<String> queryWords = morphologyService.decomposeTextToLemmasWithRank(query).keySet().stream().toList();
        List<LemmaEntity> queryLemmas = new ArrayList<>();
          for (String word : queryWords) {
            LemmaEntity lemma = lemmaRepo.getReferenceById(lemmaRepo.findByLemmaAndSiteId(word, siteId));
             if (lemma.getFrequency() < lemmaRepo.count() / 2) { //2 is used for percentage example
                queryLemmas.add(lemma);
             }
          }
        queryLemmas.sort(Comparator.comparing(LemmaEntity::getFrequency).reversed());
        LemmaEntity startingLemma = queryLemmas.get(0);

        List<PageEntity> pageList = pageRepo.getByListOfIds(indexRepo.findAllPagesByLemmaId(startingLemma.getSiteId()));
        for (LemmaEntity queryLemma : queryLemmas) {
            pageList.removeIf(page -> !indexRepo.
                    findAllLemmaIdByPageId(page.getId()).contains(queryLemma.getId()));
        }
        for (PageEntity entity : pageList) {
            String html = entity.getContent();
            List<String> snippets = getSnippets(queryWords, html);
        }
        return responses;
    };
    public List<String> getSnippets(List<String> queryWords, String html) {
        List<String> snippets = new ArrayList<>();
        List<Element> elements = new ArrayList<>();
        for (String query : queryWords) {
            //System.out.println(query);
            elements.addAll(Jsoup.parse(html).getElementsContainingText(query));
            elements.addAll(Jsoup.parse(html).getElementsMatchingText(query));
            if (elements.isEmpty()) {
                List<Element> elementsList = Jsoup.parse(html).getAllElements();
                for (Element el : elementsList) {
                    if (morphologyService.processWholeText(el.text()).contains(query)) {
                        elements.add(el);
                    };
                }
            }
            System.out.println(elements.size());
        }
      for (String query : queryWords) {
          elements.removeIf(element1 -> !morphologyService.processWholeText(element1.text()).contains(query));
          System.out.println(elements.size());
      }
        for (Element element : elements) {
            String text = element.text();
            String[] words = text.split(" ");
            StringBuffer buffer = new StringBuffer();
            for (String word : words) {
                    for (String query : queryWords) {
                    boolean wordContainsQuery = morphologyService.processWord(word).matches(query);
                       if (wordContainsQuery) {
                          word = "<b>" + word + "<b>";
                       };
                    }
              buffer.append(word + " ");
            }
            snippets.add(buffer.toString());
        }
            return snippets;
        }
    public static void main(String[] args) {
        String query = "Чехлы для смартфона";
        SearchService service = new SearchService();
        String html = service.connectionService.getContent("https://www.playback.ru");
        List<String> queryWords = service.morphologyService.decomposeTextToLemmasWithRank(query).keySet().stream().toList();
        List<String> snippets = service.getSnippets(queryWords, html);
        for (String snippet : snippets) {
            System.out.println(snippet);
        }
    }
}


