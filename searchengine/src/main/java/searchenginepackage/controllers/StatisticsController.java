package searchenginepackage.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import searchenginepackage.dto.statistics.StatisticsResponse;
import searchenginepackage.services.StatisticsServiceImpl;

@Controller
public class StatisticsController {
    @Autowired
    private StatisticsServiceImpl statisticsService;
    @ResponseBody
    @GetMapping("/api/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
