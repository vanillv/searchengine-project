package searchenginepackage.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
    private final PageRepository pageRepo;
    private final SiteRepository siteRepo;
    private final LemmaRepository lemmaRepo;
    private final IndexRepository indexRepo;

    @Autowired
    public IndexService(PageRepository pageRepo, SiteRepository siteRepo,
                        LemmaRepository lemmaRepo, IndexRepository indexRepo) {
        this.pageRepo = pageRepo;
        this.siteRepo = siteRepo;
        this.lemmaRepo = lemmaRepo;
        this.indexRepo = indexRepo;
    }
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
                siteRepo.delete(site);
                pageRepo.deleteAll(pageRepo.findBySiteId(Long.valueOf(site.getId())));
                System.out.println("Сайт удален");
            }
        } catch (NullPointerException nullPointer) {
            log.info("no sites to be deleted");
        };
    }
    private boolean indexSite(String path, PageRepository pageRep, SiteRepository siteRep) {
        try {
            deleteSiteInfo(path);
            SiteEntity site = new SiteEntity(path, connectionService.getFileName(), IndexStatus.INDEXING);
            siteRep.save(site);
            log.info(path);
            String[] map = connectionService.getMap(path).split("\n", appConfig.getMaxPagesPerSite());
            int siteId = site.getId();
            for (String pageAdress : map) {
                log.info("indexing page: " + pageAdress);
                String content = connectionService.getContent(pageAdress);
                PageEntity page = new PageEntity(siteId, path.split("/", 1)[1],
                        content, connectionService.getHttpCode(path));
                pageRep.saveAndFlush(page);
                saveLemmas(page);
            }
            siteRep.saveAndFlush(site);
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
                        indexSite(site, pageRepo, siteRepo);
                        log.info("Started indexing site: " + site);
                    }
                    log.info("task ended");
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
