package pl.pw.edu.po.search_engine.simplesearchengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlResult;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.model.CrawlHistory;
import pl.pw.edu.po.search_engine.simplesearchengine.repository.CrawlHistoryRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service responsible for web crawling functionality.
 * Supports both synchronous and asynchronous crawling modes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    private final DocumentService documentService;
    private final CrawlHistoryRepository crawlHistoryRepository;

    private static final int TIMEOUT_MS = 30000;
    private static final String USER_AGENT = "SimpleSearchEngineBot/1.0";
    private static final int MIN_CONTENT_LENGTH = 100;
    private static final long MAX_CRAWL_DURATION_MS = 3600000; // 1 hour max per crawl
    private final Map<Long, Boolean> activeCrawls = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Start crawling asynchronously in background.
     * This method is executed in a separate thread managed by Spring's task executor.
     *
     * @param request the crawl configuration
     * @param historyId the ID of the crawl history record to update
     */
    @Async("taskExecutor")
    public void crawlAsync(CrawlRequest request, Long historyId) {
        CrawlHistory history = crawlHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("CrawlHistory not found: " + historyId));
        activeCrawls.put(historyId, true);

        try {
            crawlInternal(request, history);
        } finally {
            activeCrawls.remove(historyId);
        }
    }

    /**
     * Start crawling synchronously.
     * This method blocks until crawling is complete and returns the result.
     *
     * @param request the crawl configuration
     * @return crawl result with statistics and any errors
     */
    public CrawlResult crawl(CrawlRequest request) {
        long startTime = System.currentTimeMillis();

        // Validate URL
        String validationError = validateUrl(request.getStartUrl());
        if (validationError != null) {
            log.error("URL validation failed: {}", validationError);
            return buildErrorResult(startTime, List.of(validationError));
        }

        // Create crawl history record
        CrawlHistory history = createCrawlHistory(request.getStartUrl());
        log.info("Crawl history saved with ID: {}", history.getId());

        return crawlInternal(request, history);
    }

    /**
     * Validates URL format and non-emptiness.
     *
     * @param url the URL to validate
     * @return error message if invalid, null if valid
     */
    private String validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "Start URL is null or empty";
        }

        try {
            // Validate URL format by attempting to parse it
            java.net.URL parsedUrl = new java.net.URI(url).toURL();
            // Check if URL is valid (not null and has proper format)
            if (parsedUrl.getProtocol() == null || parsedUrl.getHost() == null) {
                return "Invalid URL: missing protocol or host";
            }
            return null;
        } catch (Exception e) {
            return "Invalid URL format: " + e.getMessage();
        }
    }

    /**
     * Creates and saves a new crawl history record with STARTED status.
     *
     * @param startUrl the starting URL for the crawl
     * @return saved crawl history entity
     */
    private CrawlHistory createCrawlHistory(String startUrl) {
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
     * Internal method that performs the actual crawling work.
     * Called by both sync and async versions.
     */
    private CrawlResult crawlInternal(CrawlRequest request, CrawlHistory history) {
        long startTime = System.currentTimeMillis();

        log.info("Starting crawler for URL: {}", request.getStartUrl());
        log.info("Settings: maxPages={}, maxDepth={}, delayMs={}",
                request.getMaxPages(), request.getMaxDepth(), request.getDelayMs());

        // Queue of URLs to crawl (BFS)
        Queue<UrlWithDepth> urlQueue = new LinkedList<>();

        Set<String> visitedUrls = new HashSet<>();
        List<String> errors = new ArrayList<>();

        int pagesProcessed = 0;
        int documentsIndexed = 0;

        urlQueue.add(new UrlWithDepth(request.getStartUrl(), 0));

        while (!urlQueue.isEmpty() && pagesProcessed < request.getMaxPages()) {
            if (isCrawlCancelled(history.getId())) {
                String cancelMsg = "Crawl cancelled by user";
                errors.add(cancelMsg);
                log.info(cancelMsg);
                break;
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > MAX_CRAWL_DURATION_MS) {
                String timeoutMsg = String.format("Crawl timeout after %d ms (limit: %d ms)",
                                                 elapsedTime, MAX_CRAWL_DURATION_MS);
                errors.add(timeoutMsg);
                log.warn(timeoutMsg);
                break;
            }

            UrlWithDepth current = urlQueue.poll();
            String url = current.url;
            int depth = current.depth;

            if (visitedUrls.contains(url)) {
                continue;
            }

            visitedUrls.add(url);

            log.info("Crawling [depth={}]: {}", depth, url);

            try {
                if (pagesProcessed > 0) {
                    Thread.sleep(request.getDelayMs());
                }

                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .get();

                pagesProcessed++;

                String title = doc.title();
                String content = doc.body().text();

                // Process document if it has sufficient content
                if (content.length() > MIN_CONTENT_LENGTH) {
                    indexDocument(url, title, content);
                    documentsIndexed++;

                    log.info("Indexed: {} ({})", title, url);
                } else {
                    log.warn("Skipped (too short): {}", url);
                }

                // Extract and queue links if depth limit not reached
                if (depth < request.getMaxDepth()) {
                    int linksAdded = extractAndQueueLinks(doc, request.getStartUrl(), urlQueue, depth);
                    log.debug("Found {} valid links at depth {}", linksAdded, depth);
                }

            } catch (IOException e) {
                String error = "Failed to fetch " + url + ": " + e.getMessage();
                errors.add(error);
                log.error("Error: {}", error);
            } catch (InterruptedException e) {
                log.warn("Crawling interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Finalize crawl
        long crawlTimeMs = System.currentTimeMillis() - startTime;

        // Check if crawl was cancelled
        boolean wasCancelled = errors.stream()
                .anyMatch(e -> e.contains("cancelled"));

        String status = wasCancelled ? "CANCELLED" : determineStatus(errors.isEmpty(), documentsIndexed);

        log.info("Crawl finished: {} pages, {} indexed, {} errors in {}ms",
                pagesProcessed, documentsIndexed, errors.size(), crawlTimeMs);

        updateCrawlHistory(history, status, pagesProcessed, documentsIndexed, crawlTimeMs, errors);

        return buildCrawlResult(status, pagesProcessed, documentsIndexed, errors, crawlTimeMs);
    }

    /**
     * Indexes a document by adding it to the search engine.
     *
     * @param url document URL
     * @param title document title
     * @param content document content
     */
    private void indexDocument(String url, String title, String content) {
        DocumentRequest docRequest = DocumentRequest.builder()
                .title(title)
                .content(content)
                .url(url)
                .build();
        documentService.addOrUpdateDocument(docRequest);
    }

    /**
     * Extracts links from a document and adds them to the crawl queue.
     *
     * @param doc the Jsoup document
     * @param startUrl the starting URL (for validation)
     * @param urlQueue the queue to add URLs to
     * @param currentDepth the current crawl depth
     * @return number of links added to queue
     */
    private int extractAndQueueLinks(Document doc, String startUrl, Queue<UrlWithDepth> urlQueue, int currentDepth) {
        Elements links = doc.select("a[href]");
        int addedCount = 0;

        for (Element link : links) {
            String linkUrl = link.absUrl("href");
            if (isValidUrl(linkUrl, startUrl)) {
                urlQueue.add(new UrlWithDepth(linkUrl, currentDepth + 1));
                addedCount++;
            }
        }

        return addedCount;
    }

    /**
     * Determines the final status of the crawl based on errors and indexed documents.
     *
     * @param noErrors true if no errors occurred
     * @param documentsIndexed number of documents successfully indexed
     * @return status string (SUCCESS, PARTIAL, or FAILED)
     */
    private String determineStatus(boolean noErrors, int documentsIndexed) {
        if (noErrors) {
            return "SUCCESS";
        }
        return documentsIndexed > 0 ? "PARTIAL" : "FAILED";
    }

    /**
     * Updates the crawl history record with final results.
     *
     * @param history the crawl history entity
     * @param status final status
     * @param pagesCrawled number of pages crawled
     * @param documentsIndexed number of documents indexed
     * @param durationMs crawl duration in milliseconds
     * @param errors list of error messages
     */
    private void updateCrawlHistory(CrawlHistory history, String status, int pagesCrawled,
                                   int documentsIndexed, long durationMs, List<String> errors) {
        history.setFinishedAt(LocalDateTime.now());
        history.setStatus(status);
        history.setPagesCrawled(pagesCrawled);
        history.setDocumentsIndexed(documentsIndexed);
        history.setDurationMs(durationMs);

        if (!errors.isEmpty()) {
            history.setErrorMessage(String.join("; ", errors));
        }

        crawlHistoryRepository.save(history);
        log.info("Crawl history updated: status={}, pages={}, indexed={}", status, pagesCrawled, documentsIndexed);
    }

    /**
     * Builds a CrawlResult object with statistics and errors.
     */
    private CrawlResult buildCrawlResult(String status, int pagesProcessed, int documentsIndexed,
                                        List<String> errors, long crawlTimeMs) {
        return CrawlResult.builder()
                .status(status)
                .pagesProcessed(pagesProcessed)
                .documentsIndexed(documentsIndexed)
                .errorCount(errors.size())
                .errors(errors)
                .crawlTimeMs(crawlTimeMs)
                .build();
    }

    /**
     * Validates if a URL should be crawled.
     * Filters out:
     * - URLs from different domains
     * - File downloads (PDF, ZIP, images, etc.)
     * - URL fragments (#section)
     *
     * @param url the URL to validate
     * @param startUrl the starting URL (for domain comparison)
     * @return true if URL should be crawled, false otherwise
     */
    private boolean isValidUrl(String url, String startUrl) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        try {
            java.net.URL urlObj = new java.net.URI(url).toURL();
            java.net.URL startUrlObj = new java.net.URI(startUrl).toURL();

            // Only crawl URLs from the same domain
            if (!urlObj.getHost().equals(startUrlObj.getHost())) {
                return false;
            }

            // Filter out file downloads
            if (isFileDownload(urlObj.getPath())) {
                return false;
            }

            // Filter out URL fragments
            return urlObj.getRef() == null;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a URL path points to a file download.
     *
     * @param path the URL path
     * @return true if it's a file download, false otherwise
     */
    private boolean isFileDownload(String path) {
        String lowerPath = path.toLowerCase();
        String[] excludedExtensions = {
                ".pdf", ".zip", ".jpg", ".jpeg", ".png", ".gif",
                ".doc", ".docx", ".xls", ".xlsx", ".mp3", ".mp4"
        };

        for (String ext : excludedExtensions) {
            if (lowerPath.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds an error result for failed crawling attempts (validation errors).
     *
     * @param startTime crawl start time in milliseconds
     * @param errors list of error messages
     * @return CrawlResult with FAILED status
     */
    private CrawlResult buildErrorResult(long startTime, List<String> errors) {
        long crawlTimeMs = System.currentTimeMillis() - startTime;

        return CrawlResult.builder()
                .status("FAILED")
                .pagesProcessed(0)
                .documentsIndexed(0)
                .errorCount(errors.size())
                .errors(errors)
                .crawlTimeMs(crawlTimeMs)
                .build();
    }

    /**
     * Checks if a crawl has been cancelled.
     *
     * @param historyId the crawl history ID
     * @return true if cancelled, false if still active or not tracked (synchronous calls)
     */
    private boolean isCrawlCancelled(Long historyId) {
        Boolean isActive = activeCrawls.get(historyId);
        return isActive != null && !isActive;
    }

    /**
     * Cancel a running crawl.
     * Sets the cancellation flag for the crawl thread to check.
     *
     * @param historyId the ID of the crawl to cancel
     * @return true if crawl was found and cancelled, false if not found
     */
    public boolean cancelCrawl(Long historyId) {
        Boolean isActive = activeCrawls.get(historyId);

        if (isActive != null && isActive) {
            log.info("Cancelling crawl with ID: {}", historyId);
            activeCrawls.put(historyId, false);

            crawlHistoryRepository.findById(historyId).ifPresent(history -> {
                if ("STARTED".equals(history.getStatus())) {
                    history.setStatus("CANCELLED");
                    history.setFinishedAt(java.time.LocalDateTime.now());
                    crawlHistoryRepository.save(history);
                }
            });

            return true;
        }

        log.warn("Cannot cancel crawl {}: not found or already finished", historyId);
        return false;
    }

    /**
     * Helper record to store URL with its depth level.
     * Used for BFS (Breadth-First Search) crawling.
     *
     * @param url the URL to crawl
     * @param depth the depth level in the crawl tree
     */
    private record UrlWithDepth(String url, int depth) {}
}
