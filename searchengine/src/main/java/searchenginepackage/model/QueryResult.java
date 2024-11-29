package searchenginepackage.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class QueryResult {
    private boolean result;
    private Integer count = 0;
    private List<SiteResult> data = new ArrayList<>();

    @Override
    public String toString() {
        return "{" +
                "result=" + result +
                ", count=" + count +
                ", data=" + data +
                '}';
    }
}
