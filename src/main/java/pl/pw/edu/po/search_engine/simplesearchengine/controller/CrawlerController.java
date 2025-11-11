package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlResult;
import pl.pw.edu.po.search_engine.simplesearchengine.service.CrawlerService;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Slf4j
public class CrawlerController {

    private final CrawlerService crawlerService;

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
}
