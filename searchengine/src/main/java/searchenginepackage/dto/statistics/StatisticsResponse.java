package searchenginepackage.dto.statistics;

import lombok.Data;

@Data
public class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;
}
