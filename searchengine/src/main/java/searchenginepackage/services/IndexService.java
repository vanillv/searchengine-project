package searchenginepackage.services;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchenginepackage.config.AppConfig;
import searchenginepackage.entities.*;
import searchenginepackage.model.IndexStatus;
import searchenginepackage.repositories.*;
import searchenginepackage.responses.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


@Service
public class IndexService {
    @Autowired
    private PageRepository pageRepo;
    @Autowired
    private SiteRepository siteRepo;
    @Autowired
    private LemmaRepository lemmaRepo;
    @Autowired
    private IndexRepository indexRepo;
    private MorphologyService morphologyService = new MorphologyService();
    private ConnectionService connectionService = new ConnectionService();
    private AppConfig appConfig = AppConfig.getInstance();
    private int threadsForSites = appConfig.getThreadsForSites();
    private ExecutorService executorService = Executors.newFixedThreadPool(threadsForSites);
    private Logger log = LoggerFactory.getLogger(IndexService.class);
    private String lastError;
    private volatile boolean stopIndexing = false;
    public void deleteSiteInfo(String urlToFind) {
        try {
            if (siteRepo.findIdByUrl(urlToFind) != null) {
                SiteEntity site = siteRepo.getReferenceById(siteRepo.findIdByUrl(urlToFind));
                List<LemmaEntity> lemmaList = lemmaRepo.findAllBySiteId(site.getId());
                List<IndexEntity> indexEntities = indexRepo.findAllByLemmas(lemmaList);
                lemmaRepo.deleteAll(lemmaList);
                pageRepo.deleteAll(pageRepo.findBySiteId(site.getId()));
                siteRepo.delete(site);
                indexRepo.deleteAll(indexEntities);
            }
        } catch (NullPointerException nullPointer) {
            log.info("no sites to be deleted");
        };
    }
    private boolean indexSite(String path, PageRepository pageRep, SiteRepository siteRep) {
        String siteName = connectionService.getFileName(path);

        SiteEntity site = new SiteEntity(path, siteName , IndexStatus.INDEXING);
        try {
            deleteSiteInfo(path);
            siteRep.saveAndFlush(site);
            String[] map = connectionService.getMap(path).split("\n", appConfig.getMaxPagesPerSite());
            for (String pageAdress : map) {
                log.info("indexing page: " + pageAdress);
                String content = connectionService.getContent(pageAdress);
                PageEntity page = new PageEntity(site, pageAdress.split("/", 2)[1],
                        content, connectionService.getHttpCode(path));
                pageRep.saveAndFlush(page);
                saveLemmas(site, page);
            }
            siteRep.saveAndFlush(site);
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            lastError = e.getMessage();
            site.setStatus(IndexStatus.FAILED);
            site.setLastError(e.getMessage());
            siteRepo.saveAndFlush(site);
            log.error("Exception encountered: " + e);
            return false;
        }
    }
    private void saveLemmas(SiteEntity site, PageEntity page) {
        String content = page.getContent();
        Integer siteId = page.getSite().getId();
        Map<String, Integer> lemmaMap = morphologyService.decomposeTextToLemmasWithRank(content);
        for (String lemma : lemmaMap.keySet()) {
            float indexRank = lemmaMap.get(lemma);
            LemmaEntity lemmaEntity = lemmaRepo.findByLemmaAndSiteId(lemma, siteId);
            if (lemmaEntity == null) {
                lemmaEntity = new LemmaEntity(lemma, site, 1);
                lemmaRepo.saveAndFlush(lemmaEntity);
            } else {
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                lemmaRepo.saveAndFlush(lemmaEntity);
            }
            IndexEntity index = new IndexEntity(page, lemmaEntity, indexRank);
            indexRepo.saveAndFlush(index);
        }
    }

    public Response indexPage(String path) {
        try {
            Integer siteId;
            SiteEntity site;
            String[] siteAndPath = path.split("/", 2);
            String content = connectionService.getContent(path);
            if (siteRepo.existsById(siteRepo.findIdByUrl(siteAndPath[0]))) {
                siteId = siteRepo.findIdByUrl(siteAndPath[0]);
                site = siteRepo.getReferenceById(siteId);
                site.setStatus(IndexStatus.INDEXING);
            } else {
                site = new SiteEntity(siteAndPath[0], connectionService.getFileName(path), IndexStatus.INDEXING);
            };
            siteRepo.save(site);
            PageEntity page = new PageEntity(site, siteAndPath[1],
                    content, connectionService.getHttpCode(path));
            pageRepo.saveAndFlush(page);
            saveLemmas(site, page);
            site.setStatus(IndexStatus.INDEXED);
            siteRepo.saveAndFlush(site);
            return new Response();
        } catch (Exception e) {
            lastError = e.toString();
            log.error("new exception: " + lastError);
            System.out.println(1);
            return new Response(lastError);
        }
    }
    public synchronized Response fullIndexing() {
        if (!appConfig.isIndexingAvailable()) {
            return new Response("Indexing is already in progress.");
        }
        appConfig.setIndexingAvailable(false);
        stopIndexing = false;
        ExecutorService executorService = Executors.newFixedThreadPool(appConfig.getThreadsForSites());
        List<String> siteList = appConfig.getSites();
        try {
            int portionSize = siteList.size() / appConfig.getThreadsForSites();
            List<Future<Void>> futures = new ArrayList<>();
            for (int i = 0; i < appConfig.getThreadsForSites(); i++) {
                int start = i * portionSize;
                int end = (i == appConfig.getThreadsForSites() - 1) ? siteList.size() : (i + 1) * portionSize;
                List<String> sitePortion = siteList.subList(start, end);
                Callable<Void> task = () -> {
                    log.info("task started");
                    for (String site : sitePortion) {
                        if (stopIndexing) {
                            lastError = "Indexing stopped manually";
                            log.info(lastError);
                            return null;
                        }
                        indexSite(site, pageRepo, siteRepo);
                        log.info("Started indexing site: " + site);
                    }
                    return null;
                };
                futures.add(executorService.submit(task));
                log.info(Integer.toString(futures.size()));
                log.info("added task: " + i);
            }
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Indexing interrupted.");
                    return new Response("Indexing was interrupted.");
                } catch (ExecutionException e) {
                    log.error("Error during indexing: {}", e.getCause().getMessage());
                    return new Response("Indexing error: " + e.getCause().getMessage());
                }
            }

        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    log.info("Forcibly terminated remaining tasks.");
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("Interrupted during shutdown.");
            }
            appConfig.setIndexingAvailable(true);
            appConfig.setIndexed(true);
        }
        return stopIndexing ? new Response("Indexing stopped manually.") : new Response();
    }

    public synchronized Response stopIndexing() {
        if (!appConfig.isIndexingAvailable()) {
            stopIndexing = true;
            appConfig.setIndexingAvailable(true);
            return new Response();
        } else {
            return new Response("Indexing is not running.");
        }
    }
}
