package searchenginepackage.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchenginepackage.config.AppConfig;
import searchenginepackage.entities.*;
import searchenginepackage.model.IndexStatus;
import searchenginepackage.repositories.*;
import searchenginepackage.responses.Response;

import java.net.URL;
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
        SiteEntity site = new SiteEntity(path, siteName, IndexStatus.INDEXING);
        try {
            deleteSiteInfo(path);
            siteRep.saveAndFlush(site);
            List<String> map = connectionService.fetchAndProcessSiteMap(path);
            if (!map.isEmpty()) {
                log.info("Starting indexing for site: " + siteName + " | Total pages: " + map.size());
                for (String pageAddress : map) {
                    if (stopIndexing) {
                        log.info("Indexing stopped manually.");
                        return false;
                    }
                    log.info("Indexing page: " + pageAddress);
                    String content = morphologyService.sanitizeContent(connectionService.getContent(pageAddress));
                    URL url = new URL(pageAddress);
                    String baseUrl = url.getProtocol() + "://" + url.getHost();
                    String urlPath = url.getPath();
                    PageEntity page = new PageEntity(site, urlPath,
                            content, connectionService.getHttpCode(pageAddress));
                    pageRep.saveAndFlush(page);
                    saveLemmas(site, page);
                }
                site.setStatus(IndexStatus.INDEXED);
                siteRep.saveAndFlush(site);
                return true;
            }
        } catch (Exception e) {
            site.setStatus(IndexStatus.FAILED);
            site.setLastError(e.getMessage());
            siteRepo.saveAndFlush(site);
            log.error("Exception while indexing site: " + path, e);
            return false;
        }
        return false;
    }

    private void saveLemmas(SiteEntity site, PageEntity page) {
        String content = page.getContent();
        Map<String, Integer> lemmaMap = morphologyService.decomposeTextToLemmasWithRank(content);
        try {
            lemmaMap.forEach((lemma, frequency) -> {
                if (stopIndexing) {
                    throw new RuntimeException();
                }
                LemmaEntity lemmaEntity = lemmaRepo.findByLemmaAndSiteId(lemma, site.getId());
                if (lemmaEntity == null) {
                    lemmaEntity = new LemmaEntity(lemma, site, 1);
                } else {
                    lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                }
                lemmaRepo.saveAndFlush(lemmaEntity);
                IndexEntity index = new IndexEntity(page, lemmaEntity, frequency);
                indexRepo.saveAndFlush(index);
            });
        } catch (RuntimeException runtimeException) {log.info("lemmatization stopped");}
    }
    public Response indexPage(String page) {
        Integer siteId;
        SiteEntity site = null;
        try {
            URL url = new URL(page);
            String baseUrl = url.getProtocol() + "://" + url.getHost();
            String path = url.getPath();
            log.info("\nsite: " + baseUrl + "\npath: " + path);
            String content = connectionService.getContent(page);
            if (siteRepo.findIdByUrl(baseUrl) != null) {
                siteId = siteRepo.findIdByUrl(baseUrl);
                site = siteRepo.getReferenceById(siteId);
                log.info(site.toString());
                site.setStatus(IndexStatus.INDEXING);
            } else {
                site = new SiteEntity(baseUrl, connectionService.getFileName(page), IndexStatus.INDEXING);
            };
            siteRepo.save(site);
            PageEntity pageEntity = new PageEntity(site, path,
                    content, connectionService.getHttpCode(page));
            pageRepo.saveAndFlush(pageEntity);
            saveLemmas(site, pageEntity);
            site.setStatus(IndexStatus.INDEXED);
            siteRepo.saveAndFlush(site);
            return new Response();

        } catch (Exception e) {
            lastError = e.toString();
            site.setStatus(IndexStatus.FAILED);
            site.setLastError(e.getMessage());
            siteRepo.saveAndFlush(site);
            log.error("Exception while indexing page: " + page, e);
            return new Response(lastError);
        }
    }
    public synchronized Response fullIndexing() {
        if (!appConfig.isIndexingAvailable()) {
            return new Response("Indexing is already in progress.");
        }
        appConfig.setIndexingAvailable(false);
        appConfig.setIndexed(false);
        stopIndexing = false;
        ExecutorService executorService = Executors.newFixedThreadPool(appConfig.getThreadsForSites());
        List<String> siteList = appConfig.getSites();
        Response initialResponse = new Response();
        for (String site : siteList) {
            executorService.submit(() -> {
                if (stopIndexing) {
                    log.info("Indexing stopped manually.");
                    return;
                }
                try {
                    indexSite(site, pageRepo, siteRepo);
                    log.info("Started indexing site: " + site);
                } catch (Exception e) {
                    log.error("Error indexing site " + site, e);
                    lastError = "Error indexing site: " + e.getMessage();
                }
            });
        }
        new Thread(() -> {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    log.info("Forcibly terminated remaining tasks.");
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("Interrupted during executor shutdown.");
            } finally {
                appConfig.setIndexingAvailable(true);
                if (!stopIndexing) {
                    appConfig.setIndexed(true);
                }
                log.info("indexing available: " + appConfig.isIndexingAvailable());
                log.info("indexed: " + appConfig.isIndexed());
            }
        }).start();
        return initialResponse;
    }
    public synchronized Response stopIndexing() {
        if (!stopIndexing || appConfig.isIndexed() || !appConfig.isIndexingAvailable()) {
            stopIndexing = true;
            appConfig.isIndexingAvailable();
            return new Response();
        }
                return new Response("Indexing is not running.");
    }
}
