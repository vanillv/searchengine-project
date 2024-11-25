package searchenginepackage.config;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "config")
public class AppConfig {
    private Integer threadsForSites = 2;
    private Integer threadsForPages = 3;
    private Integer maxPagesPerSite = 200;
    private boolean indexingAvailable = true;
    private List<String> sites;
    private boolean indexed = false;
    private static AppConfig instance = new AppConfig();
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        instance.setIndexingAvailable(indexingAvailable);
        instance.setIndexed(indexed);
        instance.setThreadsForSites(threadsForSites);
        instance.setThreadsForPages(threadsForPages);
        instance.setMaxPagesPerSite(maxPagesPerSite);
        instance.setSites(sites);
    }
    public static AppConfig getInstance() {
        return instance;
    }
}