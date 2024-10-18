package searchenginepackage.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchenginepackage.entities.QueryResult;
import searchenginepackage.services.SearchService;
@RestController
public class SearchController {
    private SearchService searchService = new SearchService();
    @ResponseBody
    @GetMapping("api/search")
    public ResponseEntity<QueryResult> search(@RequestParam("query")String search) {
        String[] params = search.split(";", 4);
        String query = params[0];
        String site = "";
        int limit = 20;
        int offset = 0;
        query = params[0];
        for (String part : params) {
            if (part.matches("site =")) {
                site = part.split(" site =" ,2)[0];
            } else if (part.matches(" limit =")) {
                limit = Integer.parseInt(part.split(" limit =", 2)[0].trim());
            } else if (part.matches(" offset =")) {
                offset = Integer.parseInt(part.split(" offset =", 2)[0].trim());
            }
        }
        return ResponseEntity.ok(searchService.searchAllSites(query, site, limit, offset));
    }
}
