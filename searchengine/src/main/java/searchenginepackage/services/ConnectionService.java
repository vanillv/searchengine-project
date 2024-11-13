package searchenginepackage.services;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchenginepackage.config.AppConfig;
import searchenginepackage.model.PageLinkModel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
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
    public String getMap(String url) {
        startOfTime.set(System.currentTimeMillis());
        int numberOfThreads;
        numberOfThreads = appConfig.getThreadsForPages();
        startOfTime.set(System.currentTimeMillis());
        PageLinkModel linkModel = new PageLinkModel(url);
        String siteMap = "";
        siteMap = new ForkJoinPool(numberOfThreads).invoke(linkModel);

        long timeStop = (System.currentTimeMillis() - startOfTime.get()) / 1_000;
        System.out.printf("Обработка сайта заняла: %d секунд.%n", timeStop);

        return siteMap;
    }
    @SneakyThrows
    public String getContent(String path) {
        String pageContent = "";
        URLConnection connection = null;
            connection = new URL(path).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            pageContent = scanner.next();
            scanner.close();
        return pageContent;
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
    public List<String> createWorkingMap(String htmlMap) {
        int limit = AppConfig.getInstance().getMaxPagesPerSite();
        List<String> map = new ArrayList<>(Arrays.stream(htmlMap.split("\n")).toList());
        List<String> result = new ArrayList<>();
        int i = 0;
        for (String pageAdress : map) {
            try {
                if (isValidURL(pageAdress)) {
                    result.add(pageAdress);
                    i++;
                }
                if (i == limit) {
                    break;
                }
            } catch (Exception e) {
                log.error("Caught exception" + e.fillInStackTrace());
            }

        }
        return result;
    }
    private boolean isValidURL(String url) {
        try {
            new URL(url); // Attempts to parse the URL
            return true;
        } catch (MalformedURLException e) {
            return false; // Returns false if URL is invalid
        }
    }

}
