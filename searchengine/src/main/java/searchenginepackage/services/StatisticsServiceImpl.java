package searchenginepackage.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchenginepackage.dto.statistics.DetailedStatisticsItem;
import searchenginepackage.dto.statistics.StatisticsData;
import searchenginepackage.dto.statistics.StatisticsResponse;
import searchenginepackage.dto.statistics.TotalStatistics;
import searchenginepackage.entities.SiteEntity;
import searchenginepackage.repositories.LemmaRepository;
import searchenginepackage.repositories.PageRepository;
import searchenginepackage.repositories.SiteRepository;
import searchenginepackage.responses.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    private final Random random = new Random();
    @Autowired
    private PageRepository pageRepo;
    @Autowired
    private LemmaRepository lemmaRepo;
    @Autowired
    private SiteRepository siteRepo;

    @Override
    public StatisticsResponse getStatistics() {
        List<SiteEntity> siteList = new ArrayList<>();
        try {
            siteList = siteRepo.findAll();
        } catch (NullPointerException nullPointer) {
            log.info("siteList is empty");
        }
        int siteSize = siteList.size();
        TotalStatistics total = new TotalStatistics();
        total.setSites(siteSize);
        total.setIndexing(true);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for(int i = 0; i < siteSize; i++) {
            SiteEntity site = siteList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages =(int) pageRepo.count();
            int lemmas =(int) lemmaRepo.count();
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(site.getStatus().name());
            item.setError(site.getLastError());
            item.setStatusTime(System.currentTimeMillis());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
