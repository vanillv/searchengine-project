package searchenginepackage.model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;

public class PageLinkModel extends RecursiveTask<String> {

    private final String link;
    private static final CopyOnWriteArrayList<String> WRITE_ARRAY_LIST = new CopyOnWriteArrayList<>();
    private static final String CSS_QUERY = "a[href]";
    private static final String ATTRIBUTE_KEY = "href";

    public PageLinkModel(String link) {
        this.link = link.trim();
    }

    @Override
    protected String compute() {
        String stringUtils = StringUtils.repeat("\t",
                link.lastIndexOf("/") != link.length() - 1 ? StringUtils.countMatches(link, "/") - 2
                        : StringUtils.countMatches(link, "/") - 3);

        StringBuffer sb = new StringBuffer(String.format("%s%s%s", stringUtils, link, System.lineSeparator()));
        List<PageLinkModel> writeArrayList = new CopyOnWriteArrayList<>();
        Document document;
        Elements elements;
        try {
            Thread.sleep(150);
            document = Jsoup.connect(link).ignoreContentType(true).userAgent("Mozilla/5.0").get();
            elements = document.select(CSS_QUERY);
            for (Element element : elements) {
                String attributeUrl = element.absUrl(ATTRIBUTE_KEY);
                if (!attributeUrl.isEmpty() && attributeUrl.startsWith(link) && !WRITE_ARRAY_LIST.contains(attributeUrl) && !attributeUrl
                        .contains("#")) {
                    PageLinkModel linkExecutor = new PageLinkModel(attributeUrl);
                    linkExecutor.fork();
                    writeArrayList.add(linkExecutor);
                    WRITE_ARRAY_LIST.add(attributeUrl);
                }
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }

        writeArrayList.sort(Comparator.comparing((PageLinkModel o) -> o.link));
        int i = 0, allTasksSize = writeArrayList.size();
        while (i < allTasksSize) {
            PageLinkModel link = writeArrayList.get(i);
            sb.append(link.join());
            i++;
        }
        return sb.toString();
    }
}
