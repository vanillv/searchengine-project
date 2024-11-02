package searchenginepackage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
@Configuration
@ConfigurationProperties(prefix = "config")
public class AppConfig {
    private Integer threadsForSites = 2;
    private Integer threadsForPages = 2;
    private Integer maxPagesPerSite = 20;
    private volatile boolean indexingAvailable = true;
    private List<String> sites = new ArrayList<>();
    public static AppConfig appConfig = new AppConfig();
    private boolean initialised;
}
