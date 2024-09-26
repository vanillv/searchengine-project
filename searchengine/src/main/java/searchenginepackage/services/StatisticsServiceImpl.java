package searchenginepackage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchenginepackage.dto.statistics.DetailedStatisticsItem;
import searchenginepackage.dto.statistics.StatisticsData;
import searchenginepackage.dto.statistics.StatisticsResponse;
import searchenginepackage.dto.statistics.TotalStatistics;
import searchenginepackage.model.SiteEntity;
import searchenginepackage.repositories.LemmaRepository;
import searchenginepackage.repositories.PageRepository;
import searchenginepackage.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Service
@Component
public class StatisticsServiceImpl implements StatisticsService {
    private final Random random = new Random();
    @Autowired
    private PageRepository pageRepo;
    @Autowired
    private LemmaRepository lemmaRepo;
    @Autowired
    private SiteRepository siteRepo;
    List<SiteEntity> siteList = siteRepo.findAll();
    private final int siteSize = siteList.size();
    @Override
    public StatisticsResponse getStatistics() {
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