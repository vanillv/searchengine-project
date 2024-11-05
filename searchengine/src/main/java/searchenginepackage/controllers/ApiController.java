package searchenginepackage.controllers;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchenginepackage.model.QueryResult;
import searchenginepackage.responses.Response;
import searchenginepackage.services.IndexService;
import searchenginepackage.services.SearchService;

@Controller
@RequestMapping("/api")
public class ApiController {
    
    private IndexService indexService = new IndexService();
    private SearchService searchService = new SearchService();
    @ResponseBody
    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() {
            return ResponseEntity.ok(indexService.fullIndexing());
    }
    @ResponseBody
    @GetMapping("/stopIndexing")
    public synchronized ResponseEntity<Response> stopIndexing() {
        return ResponseEntity.ok(indexService.stopIndexing());
    }
    @ResponseBody
    @PostMapping("/indexPage")
    public synchronized ResponseEntity<Response> indexPage(@RequestParam(name = "url") String page) {
        return ResponseEntity.ok(indexService.indexPage(page));
    }

    @ResponseBody
    @GetMapping("/search")
    public ResponseEntity<QueryResult> search(@RequestParam("query")String query, @RequestParam(value = "site", required = false) String site,
                                              @RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) {
        return ResponseEntity.ok(searchService.searchAllSites(query, site, limit, offset));
    }
}
