package searchenginepackage.services;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchenginepackage.config.AppConfig;
import searchenginepackage.model.PageLinkModel;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Getter
@Setter
public class ConnectionService {
    private static final Logger log = LoggerFactory.getLogger(ConnectionService.class);
    private AppConfig appConfig = AppConfig.getInstance();
    private static final CopyOnWriteArrayList<String> WRITE_ARRAY_LIST = new CopyOnWriteArrayList<>();
    private static final String CSS_QUERY = "a[href]";
    private static final String ATTRIBUTE_KEY = "href";
    private static final AtomicLong startOfTime = new AtomicLong();
    public int getHttpCode(String path) {
        try {
            return Jsoup.connect(path).userAgent("Mozilla").timeout(5000).get().connection().response().statusCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getFileName(String url) {
        try {
            String fileName;
            fileName = new URL(url).getHost().replace(".", "_");
            return fileName;
        } catch (Exception e) {e.printStackTrace();}
       return null;
    }
    private String getMap(String url) {
        startOfTime.set(System.currentTimeMillis());
        int numberOfThreads = appConfig.getThreadsForPages();
        System.out.printf("Starting site mapping for %s with %d threads.%n", url, numberOfThreads);
        String siteMap = "";
        try {
            PageLinkModel linkModel = new PageLinkModel(url);
            siteMap = new ForkJoinPool(numberOfThreads).invoke(linkModel);
        } catch (Exception e) {
            System.err.println("Error during site mapping: " + e.getMessage());
            e.printStackTrace();
        }
        long timeStop = (System.currentTimeMillis() - startOfTime.get()) / 1_000;
        System.out.printf("Site processing took: %d seconds.%n", timeStop);
        return siteMap;
    }

    public List<String> fetchAndProcessSiteMap(String url) {
        String htmlMap = getMap(url);
        List<String> map = new ArrayList<>(Arrays.stream(htmlMap.split("\n")).toList());
        List<String> result = new ArrayList<>();
        int i = 0;
          int limit = appConfig.getMaxPagesPerSite();//for quicker tests
        for (String pageAdress : map) {
            try {
                if (isValidURL(pageAdress)) {
                    result.add(pageAdress);
                    i++;
                }
                  if (i == limit) {  //same
                    break;
                  }
            } catch (Exception e) {
                log.error("Caught exception" + e.fillInStackTrace());
            }

        }
        return result;
    }
    @SneakyThrows
    public String getContent(String path) {
        int retries = 3;
        while (retries > 0) {
            try {
                Connection.Response response = Jsoup.connect(path)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36")
                        .header("Accept-Language", "ru-RU,ru;q=0.9")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .timeout(5000)
                        .execute();
                if (response.statusCode() == 200) {
                    return response.body();
                } else {
                    log.warn("Received non-OK status code: {} for URL: {}", response.statusCode(), path);
                    return null;
                }
            } catch (IOException e) {
                log.error("Error connecting to URL: {}", path, e);
                retries--;
                if (retries > 0) {
                    log.info("Retrying... {} attempts left.", retries);
                    TimeUnit.SECONDS.sleep(2);
                } else {
                    log.error("Failed to retrieve content after multiple attempts.");
                    return null;
                }
            }
        }
        return null;
    }
    @SneakyThrows
    public String getBody(String html) {
            Document doc = Jsoup.parse(html);
            return doc.body().html();
    }
    @SneakyThrows
    public String getTitle(String html) {
            Document doc = Jsoup.parse(html);
            return doc.title();
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
