package searchenginepackage.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchenginepackage.config.AppConfig;
import searchenginepackage.dto.statistics.StatisticsResponse;
import searchenginepackage.model.QueryResponse;
import searchenginepackage.model.QueryResult;
import searchenginepackage.responses.Response;
import searchenginepackage.services.IndexService;
import searchenginepackage.services.SearchService;
import searchenginepackage.services.StatisticsServiceImpl;

@RestController
@RequestMapping("/api")
public class ApiController {

    private StatisticsServiceImpl statisticsService = new StatisticsServiceImpl();
    private IndexService indexService = new IndexService();
    private SearchService searchService = new SearchService();

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/search")
    public ResponseEntity<QueryResult> search(@RequestParam("query")String query, @RequestParam("site") String site,
                                                @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        //make a simple requestParameter String that splits by ","
        String[] params = query.split(";");

        int defaultOffset = 0;
        int defaultLimit = 20;
        if (limit == 0) {
            limit = defaultLimit;
        }
        if (offset < 1) {
            offset = defaultOffset;
        }
        return ResponseEntity.ok(new QueryResult());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() {
            //Response instead of void
            return ResponseEntity.ok(indexService.FullIndexing());
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        return ResponseEntity.ok(indexService.stopIndexing());
    }
    @GetMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@RequestParam String page) {
        return ResponseEntity.ok(indexService.indexPage(page));
    }
    //● Разбивать поисковый запрос на отдельные слова и формировать
    //из этих слов список уникальных лемм, исключая междометия,
    //союзы, предлоги и частицы. Используйте для этого код, который
    //вы уже писали в предыдущем этапе.
    //● Исключать из полученного списка леммы, которые встречаются на
    //слишком большом количестве страниц. Поэкспериментируйте и
    //определите этот процент самостоятельно.
    //● Сортировать леммы в порядке увеличения частоты встречаемости
    //(по возрастанию значения поля frequency) — от самых редких до
    //самых частых.
    //● По первой, самой редкой лемме из списка, находить все страницы,
    //на которых она встречается. Далее искать соответствия
    //следующей леммы из этого списка страниц, а затем повторять
    //операцию по каждой следующей лемме. Список страниц при этом
    //на каждой итерации должен уменьшаться.
    //● Если в итоге не осталось ни одной страницы, то выводить пустой
    //список.
    //● Если страницы найдены, рассчитывать по каждой из них
    //релевантность (и выводить её потом, см. ниже) и возвращать.
    //● Для каждой страницы рассчитывать абсолютную релевантность —
    //сумму всех rank всех найденных на странице лемм (из таблицы
    //index), которая делится на максимальное значение этой
    //абсолютной релевантности для всех найденных страниц.

}
