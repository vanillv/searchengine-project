package searchenginepackage.model;

import lombok.Data;

@Data
public class SingleResult {
    private String site;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
    public SingleResult(String site, String uri, String title, String snippet, float relevance) {
        this.site = site;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
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
