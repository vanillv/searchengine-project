package searchenginepackage.controllers;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchenginepackage.model.PageRequest;
import searchenginepackage.model.QueryResult;
import searchenginepackage.responses.Response;
import searchenginepackage.services.IndexService;
import searchenginepackage.services.SearchService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api")
public class ApiController {
    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
    @Autowired
    private IndexService indexService;
    @Autowired
    private SearchService searchService;
    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() {
            return ResponseEntity.ok(indexService.fullIndexing());
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        return ResponseEntity.ok(indexService.stopIndexing());
    }
    @PostMapping("/indexPage")
    @ResponseBody
    public ResponseEntity<Response> indexPage(HttpServletRequest request) {
        log.info(request.getContentType());
        try {
            log.info("Body: {}", request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String pageEntity = request.getParameter("pageEntity");
        log.info("pageEntity parameter: {}", pageEntity);

        if (pageEntity == null || pageEntity.isEmpty()) {
            return ResponseEntity.badRequest().body(new Response("Invalid page entity."));
        }
        return ResponseEntity.ok(indexService.indexPage(""));
    }
    @ResponseBody
    @GetMapping("/search")
    public ResponseEntity<QueryResult> search(@RequestParam("query")String query, @RequestParam(value = "site", required = false) String site,
                                              @RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) {
        return ResponseEntity.ok(searchService.searchAllSites(query, site, limit, offset));
    }
}
