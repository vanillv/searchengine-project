package searchenginepackage.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import searchenginepackage.dto.statistics.StatisticsResponse;
import searchenginepackage.services.StatisticsServiceImpl;

@Controller
public class StatisticsController {
    private StatisticsServiceImpl statisticsService = new StatisticsServiceImpl();
    @ResponseBody
    @GetMapping("/api/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
