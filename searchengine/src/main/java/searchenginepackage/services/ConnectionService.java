package searchenginepackage.services;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import searchenginepackage.config.AppConfig;
import searchenginepackage.model.PageLinkModel;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Getter
@Setter
public class ConnectionService {

    private static final CopyOnWriteArrayList<String> WRITE_ARRAY_LIST = new CopyOnWriteArrayList<>();
    private static final String CSS_QUERY = "a[href]";
    private static final String ATTRIBUTE_KEY = "href";
    private String fileName;
    private static final AtomicLong startOfTime = new AtomicLong();

    public int getHttpCode(String path) {
        try {
            return Jsoup.connect(path).userAgent("Mozilla").timeout(5000).get().connection().response().statusCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getMap(String url) {
        startOfTime.set(System.currentTimeMillis());
        try {
            fileName = new URL(url).getHost().replace(".", "_");
            System.out.println("Название: " + fileName);
        } catch (Exception e) {e.printStackTrace();}

        int numberOfThreads;
        numberOfThreads = AppConfig.appConfig.getThreadsForSites();
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
    private String getSnippet(String html, String query) {
        //temporary
        Document doc = Jsoup.parse(html);
        return doc.html();
    }
}
