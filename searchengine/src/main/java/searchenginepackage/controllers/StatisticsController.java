package searchenginepackage.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchenginepackage.dto.statistics.StatisticsResponse;
import searchenginepackage.services.StatisticsServiceImpl;

@RestController
public class StatisticsController {
    private StatisticsServiceImpl statisticsService = new StatisticsServiceImpl();
    @ResponseBody
    @GetMapping("/api/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
