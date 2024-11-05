package searchenginepackage.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "config")
public class AppConfig {
    private static AppConfig instance;
    @PostConstruct
    public void init() {
        instance = this;
    }
    private Integer threadsForSites = 2;
    private Integer threadsForPages = 2;
    private Integer maxPagesPerSite = 20;
    private volatile boolean indexingAvailable = true;
    @Value("{config.sites}")
    private List<String> sites;
    private boolean indexed = false;
    public static AppConfig getInstance() {
        return instance;
    }
    public static Integer getThreadsForSites() {
        return instance.threadsForSites;
    }
    public static Integer getThreadsForPages() {
        return instance.threadsForPages;
    }
    public static Integer getMaxPagesPerSite() {
        return instance.maxPagesPerSite;
    }
    public static boolean isIndexingAvailable() {
        return instance.indexingAvailable;
    }
    public static List<String> getSites() {
        return instance.sites;
    }
}
