package searchenginepackage.model;

import lombok.Data;

import java.util.List;

@Data
public class QueryResponse {
    private String site;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
    public QueryResponse(String site, String uri, String title, String snippet, float relevance) {

    }

    @Override
    public String toString() {
        return "{" +
                "site='" + site + '\'' +
                ", title='" + title + '\'' +
                ", uri='" + uri + '\'' +
                ", snippet='" + snippet + '\'' +
                ", relevance=" + relevance +
                '}';
    }
}
