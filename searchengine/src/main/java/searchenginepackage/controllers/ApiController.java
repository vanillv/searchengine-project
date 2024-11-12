package searchenginepackage.controllers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
    @Autowired
    private IndexService indexService;
    @Autowired
    private SearchService searchService;
    @ResponseBody
    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() {
            return ResponseEntity.ok(indexService.fullIndexing());
    }
    @ResponseBody
    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        return ResponseEntity.ok(indexService.stopIndexing());
    }
    @PostMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@RequestBody String page) {
        return ResponseEntity.ok(indexService.indexPage(page));
    }

    @ResponseBody
    @GetMapping("/search")
    public ResponseEntity<QueryResult> search(@RequestParam("query")String query, @RequestParam(value = "site", required = false) String site,
                                              @RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) {
        return ResponseEntity.ok(searchService.searchAllSites(query, site, limit, offset));
    }
}
