package searchenginepackage.model;

import lombok.Data;

@Data
public class SingleResult {
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
    public SingleResult(String siteName, String uri, String title, String snippet, float relevance) {
        this.siteName = siteName;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }

    @Override
    public String toString() {
        return "{" +
                "site='" + siteName + '\'' +
                ", title='" + title + '\'' +
                ", uri='" + uri + '\'' +
                ", snippet='" + snippet + '\'' +
                ", relevance=" + relevance +
                '}';
    }
}
