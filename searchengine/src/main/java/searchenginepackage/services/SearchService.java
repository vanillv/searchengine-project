package searchenginepackage.services;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private SearchService init(PageRepository pageRepository, SiteRepository siteRepository,
                              LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        pageRepo = pageRepository; siteRepo = siteRepository; lemmaRepo = lemmaRepository;
        indexRepo = indexRepository;
        return this;
    }
    public QueryResult searchAllSites(String query, String site, int offset, int limit) {
        QueryResult queryResult = new QueryResult();
        if(!site.isEmpty()) {

        }
        queryResult.getData().addAll(searchSite(query, site));

        return queryResult;
    }
    public List<QueryResponse> searchSite(String query, String site) {
        List<QueryResponse> responses = new ArrayList<>();
        SiteEntity siteEntity = siteRepo.getReferenceById(siteRepo.findByUrl(site));
        Integer siteId = siteEntity.getId();
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

        List<PageEntity> pageList = indexRepo.findAllPagesByLemmaId(startingLemma.getSiteId());
        for (LemmaEntity lemma : queryLemmas) {
            for (PageEntity page : pageList) {

            }
        }
        return responses;
    };

}
