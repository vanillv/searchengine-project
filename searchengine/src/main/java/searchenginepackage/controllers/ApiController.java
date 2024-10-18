package searchenginepackage.controllers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchenginepackage.responses.Response;
import searchenginepackage.services.IndexService;
@RestController
@RequestMapping("/api")
public class ApiController {
    private IndexService indexService = new IndexService();
    @ResponseBody
    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() {
            return ResponseEntity.ok(indexService.FullIndexing());
    }
    @ResponseBody
    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        return ResponseEntity.ok(indexService.stopIndexing());
    }
    @ResponseBody
    @PutMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@RequestParam String page) {
        return ResponseEntity.ok(indexService.indexPage(page));
    }
}
