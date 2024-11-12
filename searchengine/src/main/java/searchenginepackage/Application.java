package searchenginepackage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import searchenginepackage.config.AppConfig;

@SpringBootApplication
@EnableJpaRepositories
@EnableConfigurationProperties(AppConfig.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = {"searchenginepackage", "searchenginepackage.repositories"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
