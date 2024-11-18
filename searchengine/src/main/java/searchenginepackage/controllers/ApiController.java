package searchenginepackage.controllers;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchenginepackage.config.AppConfig;
import searchenginepackage.model.QueryResult;
import searchenginepackage.responses.Response;
import searchenginepackage.services.IndexService;
import searchenginepackage.services.SearchService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
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
      Response indexingResult = indexService.fullIndexing();
      if (indexingResult.isResult()) {
          return ResponseEntity.ok(indexingResult);
      } else return ResponseEntity.badRequest().body(indexingResult);
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        Response stopIndexingResult = indexService.stopIndexing();
        if (stopIndexingResult.isResult()) {
            return ResponseEntity.ok(stopIndexingResult);
        } else return ResponseEntity.badRequest().body(stopIndexingResult);
    }
    @PostMapping("/indexPage")
    @ResponseBody
    public ResponseEntity<Response> indexPage(@RequestBody String page) {
        String decodedUrl = "";
        try {
            decodedUrl = URLDecoder.decode(page, "UTF-8").replace("url=", "");
            log.info(decodedUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (decodedUrl == null || decodedUrl.isEmpty()) {
            return ResponseEntity.badRequest().body(new Response("Empty page adress"));
        }
        Response response = indexService.indexPage(decodedUrl);
        if (response.isResult()) {
            return ResponseEntity.ok(response);
        }return ResponseEntity.badRequest().body(response);
    }
    @ResponseBody
    @GetMapping("/search")
    public ResponseEntity<QueryResult> search(@RequestParam("query")String query, @RequestParam(value = "site", required = false) String site,
                                              @RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) {
        QueryResult result = searchService.searchAllSites(query, site, limit, offset);
        if (result.isResult()) {
            return ResponseEntity.ok(searchService.searchAllSites(query, site, limit, offset));
        }
        return ResponseEntity.badRequest().body(result);
    }
}
