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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for web crawler operations.
 * Provides endpoints for starting crawls and viewing crawl history.
 */
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Slf4j
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final CrawlHistoryRepository crawlHistoryRepository;

    /**
     * Start crawling synchronously.
     * This endpoint blocks until crawling is complete.
     *
     * @param request crawl configuration
     * @return crawl result with statistics
     */
    @PostMapping("/start")
    public ResponseEntity<CrawlResult> startCrawling(@RequestBody CrawlRequest request) {
        log.info("Starting synchronous crawl: {}", request.getStartUrl());

        CrawlResult result = crawlerService.crawl(request);

        log.info("Crawl completed: status={}, pages={}, indexed={}",
                result.getStatus(), result.getPagesProcessed(), result.getDocumentsIndexed());

        return ResponseEntity.ok(result);
    }

    /**
     * Start crawling asynchronously.
     * Returns immediately with HTTP 202 Accepted and a crawl history ID.
     * Use GET /api/crawler/history/{id} to check progress.
     *
     * @param request crawl configuration
     * @return HTTP 202 with crawl ID and status URL
     */
    @PostMapping("/start-async")
    public ResponseEntity<?> startCrawlingAsync(@RequestBody CrawlRequest request) {
        log.info("Starting asynchronous crawl: {}", request.getStartUrl());

        // Validate URL
        String validationError = validateCrawlRequest(request);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(Map.of("error", validationError));
        }

        // Create history record with STARTED status
        CrawlHistory history = createInitialCrawlHistory(request.getStartUrl());
        log.info("Created crawl history with ID: {}", history.getId());

        // Start crawling in background thread
        crawlerService.crawlAsync(request, history.getId());

        // Return immediately
        return ResponseEntity.accepted().body(Map.of(
                "message", "Crawling started in background",
                "crawlId", history.getId(),
                "statusUrl", "/api/crawler/history/" + history.getId()
        ));
    }

    /**
     * Validates the crawl request.
     *
     * @param request the request to validate
     * @return error message if invalid, null if valid
     */
    private String validateCrawlRequest(CrawlRequest request) {
        if (request.getStartUrl() == null || request.getStartUrl().trim().isEmpty()) {
            return "Start URL is required";
        }

        try {
            // Validate URL format by parsing it
            java.net.URL parsedUrl = new java.net.URI(request.getStartUrl()).toURL();
            if (parsedUrl.getProtocol() == null || parsedUrl.getHost() == null) {
                return "Invalid URL: missing protocol or host";
            }
            return null;
        } catch (Exception e) {
            return "Invalid URL format: " + e.getMessage();
        }
    }

    /**
     * Creates an initial crawl history record with STARTED status.
     *
     * @param startUrl the starting URL
     * @return saved crawl history entity
     */
    private CrawlHistory createInitialCrawlHistory(String startUrl) {
        CrawlHistory history = CrawlHistory.builder()
                .startUrl(startUrl)
                .startedAt(LocalDateTime.now())
                .status("STARTED")
                .pagesCrawled(0)
                .documentsIndexed(0)
                .build();
        return crawlHistoryRepository.save(history);
    }

    /**
     * Get all crawl history records.
     *
     * @return list of all crawl history records
     */
    @GetMapping("/history")
    public ResponseEntity<List<CrawlHistory>> getCrawlHistory() {
        log.info("Fetching all crawl history");
        List<CrawlHistory> history = crawlHistoryRepository.findAll();
        return ResponseEntity.ok(history);
    }

    /**
     * Get a specific crawl history record by ID.
     *
     * @param id the crawl history ID
     * @return crawl history record or 404 if not found
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<CrawlHistory> getCrawlHistoryById(@PathVariable Long id) {
        log.info("Fetching crawl history for ID: {}", id);
        return crawlHistoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
