package searchenginepackage.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Service;
import searchenginepackage.entities.*;
import searchenginepackage.model.IndexStatus;
import searchenginepackage.repositories.*;
import searchenginepackage.responses.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static searchenginepackage.config.AppConfig.appConfig;

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
    private int threadsForSites = appConfig.getThreadsForSites();
    private ExecutorService executorService = Executors.newFixedThreadPool(threadsForSites);
    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    private String lastError;
    private volatile boolean stopIndexing = false;
    @Autowired
    private IndexService init(PageRepository pageRepository, SiteRepository siteRepository,
                              LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        pageRepo = pageRepository; siteRepo = siteRepository; lemmaRepo = lemmaRepository;
        indexRepo = indexRepository;
        return this;
    }
    public void deleteSiteInfo(String urlToFind) {
        try {
            if (siteRepo.findIdByUrl(urlToFind) != null) {
                SiteEntity site = siteRepo.getReferenceById(siteRepo.findIdByUrl(urlToFind));
                siteRepo.delete(site);
                pageRepo.deleteAll(pageRepo.findBySiteId(Long.valueOf(site.getId())));
                System.out.println("Сайт удален");
            }
        } catch (NullPointerException nullPointer) {
            log.info("NullPointerException during deletion of sites");
        };
    }
    private boolean indexSite(String path) {
        try {
            deleteSiteInfo(path);
            SiteEntity site = new SiteEntity(path, connectionService.getFileName(), IndexStatus.INDEXING);
            siteRepo.save(site);
            log.info("added site: " + site.getUrl());
            String[] map = connectionService.getMap(path).split("\n", appConfig.getMaxPagesPerSite());
            int siteId = site.getId();
            for (String pageAdress : map) {
                log.info("indexing page: " + pageAdress);
                String content = connectionService.getContent(pageAdress);
                PageEntity page = new PageEntity(siteId, path.split("/", 1)[1],
                        content, connectionService.getHttpCode(path));
                pageRepo.save(page);
                saveLemmas(page);
            }
            siteRepo.save(site);
            return true;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.info("Exception encountered: " + e.getMessage());
            return false;
        }
    }
    public Response indexPage(String path) {
        try {
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
                pageRepo.saveAndFlush(page);
                saveLemmas(page);
                site.setStatus(IndexStatus.INDEXED);
                siteRepo.saveAndFlush(site);
                return new Response();
          } catch (Exception e) {
            lastError = e.getMessage();
            log.info("new exception: " + lastError);
            System.out.println(1);
            return new Response(lastError);
        }

    }
    private void saveLemmas(PageEntity page) {
        String content = page.getContent();
        Integer siteId = page.getSiteId();
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
                            log.info("Indexing stopped manually.");
                            return null;
                        }
                        indexSite(site);
                        log.info("Started indexing site: " + site);
                    }
                    log.info("task ended");
                    return null;
                };
                futures.add(executorService.submit(task));
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
