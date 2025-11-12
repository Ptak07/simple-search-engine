package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlResult;
import pl.pw.edu.po.search_engine.simplesearchengine.model.CrawlHistory;
import pl.pw.edu.po.search_engine.simplesearchengine.repository.CrawlHistoryRepository;
import pl.pw.edu.po.search_engine.simplesearchengine.service.CrawlerService;

import java.util.List;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Slf4j
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final CrawlHistoryRepository crawlHistoryRepository;

    @PostMapping("/start")
    public ResponseEntity<CrawlResult> startCrawling(@RequestBody CrawlRequest request) {
        log.info("POST /api/crawler/start - Starting crawl with request: {}", request);

        CrawlResult result = crawlerService.crawl(request);

        log.info("Crawl completed: status={}, pages={}, indexed={}",
                result.getStatus(),
                result.getPagesProcessed(),
                result.getDocumentsIndexed());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CrawlHistory>> getCrawlHistory() {
        log.info("GET /api/crawler/history - Fetching crawl history");
        List<CrawlHistory> history = crawlHistoryRepository.findAll();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<CrawlHistory> getCrawlHistoryById(@PathVariable Long id) {
        log.info("GET /api/crawler/history/{} - Fetching specific crawl", id);
        return crawlHistoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
