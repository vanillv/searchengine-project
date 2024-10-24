package searchenginepackage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Service;
import searchenginepackage.config.AppConfig;
import searchenginepackage.entities.*;
import searchenginepackage.model.IndexStatus;
import searchenginepackage.repositories.*;
import searchenginepackage.responses.Response;

import java.util.List;
import java.util.Map;

@AutoConfiguration
@RequiredArgsConstructor
@Service
public class IndexService {
    private PageRepository pageRepo;
    private SiteRepository siteRepo;
    private LemmaRepository lemmaRepo;
    private IndexRepository indexRepo;
    private MorphologyService morphologyService = new MorphologyService();
    private ConnectionService connectionService = new ConnectionService();
    boolean indexingIsAvailable = AppConfig.appConfig.isIndexingAvailable();
    private String lastError;
    private boolean stopIndexing = false;
    @Autowired
    private IndexService init(PageRepository pageRepository, SiteRepository siteRepository,
                              LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        pageRepo = pageRepository; siteRepo = siteRepository; lemmaRepo = lemmaRepository;
        indexRepo = indexRepository;
        return this;
    }
    public void deleteSiteInfo(String urlToFind) {
        if (siteRepo.findIdByUrl(urlToFind) != null) {
            SiteEntity site = siteRepo.getReferenceById(siteRepo.findIdByUrl(urlToFind));
            siteRepo.delete(site);
            pageRepo.deleteAll(pageRepo.findAllBySiteId(site.getId()));
            System.out.println("Сайт удален");
        }
    }
    private boolean indexSite(String path) {
        try {
            deleteSiteInfo(path);
            SiteEntity site = new SiteEntity(path, connectionService.getFileName(), IndexStatus.INDEXING);
            siteRepo.save(site);
            String[] map = connectionService.getMap(path).split("\n", AppConfig.appConfig.getMaxPagesPerSite());
            int siteId = site.getId();
               for (String pageAdress : map) {
                    String content = connectionService.getContent(pageAdress);
                    PageEntity page = new PageEntity(siteId, path.split("/", 1)[1],
                            content, connectionService.getHttpCode(path));
                    pageRepo.save(page);
                    saveLemmas(page);
               }
               site.setStatus(IndexStatus.INDEXED);
               siteRepo.saveAndFlush(site);
               pageRepo.flush();
               return true;
        } catch (Exception e) {
            lastError = e.getMessage();
            System.out.println("FAILED WITH EXCEPTION");
            return false;
        }
    }
    public Response indexPage(String path) {
        try {
            if (indexingIsAvailable) {
                AppConfig.appConfig.setIndexingAvailable(false);
                String[] siteAndPath = path.split("/", 2);
                String content = connectionService.getContent(path);
                Integer siteId;
                SiteEntity site;
                if (siteRepo.existsById(siteRepo.findIdByUrl(siteAndPath[0]))) {
                    siteId = siteRepo.findIdByUrl(siteAndPath[0]);
                    site = siteRepo.getReferenceById(siteId);
                    site.setStatus(IndexStatus.INDEXING);
                } else {
                    site = new SiteEntity(siteAndPath[0], connectionService.getFileName(), IndexStatus.INDEXING);
                    siteId = site.getId();
                };
                siteRepo.save(site);
                PageEntity page = new PageEntity(siteId, siteAndPath[1],
                        content, connectionService.getHttpCode(path));
                pageRepo.save(page);
                saveLemmas(page);
                site.setStatus(IndexStatus.INDEXED);
                siteRepo.save(site);
                AppConfig.appConfig.setIndexingAvailable(true);
                pageRepo.flush();
                siteRepo.flush();
                return new Response();

            } else {
                lastError = "Indexing is not available";
            }
        } catch (Exception e) {
            lastError = e.getMessage();
        }
        return new Response(lastError);
    }
    private void saveLemmas(PageEntity page) {
        String content = page.getContent();
        Integer siteId = page.getSite_id();
        Map<String, Integer> lemmaMap = morphologyService.decomposeTextToLemmasWithRank(content);
        for (String lemma : lemmaMap.keySet()) {
            LemmaEntity lemmaEntity;
            Integer indexRank = lemmaMap.get(lemma);
            if (lemmaRepo.existsById(lemmaRepo.findIdByLemmaAndSiteId(lemma, siteId))) {
                lemmaEntity = lemmaRepo.getReferenceById(lemmaRepo.findIdByLemmaAndSiteId(lemma, siteId));
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + lemmaMap.get(lemma));
            } else {
                lemmaEntity = new LemmaEntity(lemma, siteId, 1);
            }
            IndexEntity index = new IndexEntity(page.getId(), lemmaEntity.getId(), indexRank);
            indexRepo.save(index);
            lemmaRepo.save(lemmaEntity);
        }
        indexRepo.flush();
        lemmaRepo.flush();
    }

    public Response FullIndexing() {
        if (indexingIsAvailable) {
            AppConfig.appConfig.setIndexingAvailable(false);
         int threadsForSites = AppConfig.appConfig.getThreadsForSites();
         List<String> siteList = AppConfig.appConfig.getSites();
          try {
              Thread[] threadArray = new Thread[threadsForSites];
            for (int i = 0; i < threadsForSites; i++){
                int portion = siteList.size() / threadsForSites;
                int portionStart = 0;
                int portionEnd = siteList.size() / threadsForSites;
                if (i != 0) {
                    portionStart += portion;
                    portionEnd += portion;
                }
                List<String> siteListPart = siteList.subList(portionStart, portionEnd);
                  Thread t = new Thread(new Runnable() {
                     @Override
                     public void run() {
                        for (String site : siteListPart) {
                            boolean indexed = indexSite(site) && !stopIndexing;
                            SiteEntity entity = siteRepo.getReferenceById(siteRepo.findIdByUrl(site));
                            if (indexed) {
                                entity.setStatus(IndexStatus.INDEXED);
                            } else {
                                entity.setStatus(IndexStatus.FAILED);
                                entity.setLastError(lastError);
                            }
                            siteRepo.save(entity);
                        }
                     }
                  });
                 threadArray[i] = t;
                threadArray[i].run();
                if (stopIndexing) {
                    for (Thread thr : threadArray) {
                        thr.interrupt();
                    }
                    stopIndexing = false;
                    break;
                }
            }
         } catch (Exception e) {
            return new Response("Ошибка: " + e);
          }
        }
        AppConfig.appConfig.setInitialised(true);
        AppConfig.appConfig.setIndexingAvailable(true);
        return new Response();
    }
    public Response stopIndexing() {
        if(!indexingIsAvailable) {
            stopIndexing = true;
            return new Response();
        } else return new Response("Индексизация не запущена");
    }
}
