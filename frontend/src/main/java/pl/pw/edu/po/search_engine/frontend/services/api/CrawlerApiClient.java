package pl.pw.edu.po.search_engine.frontend.services.api;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.pw.edu.po.search_engine.frontend.model.CrawlHistoryDTO;

import java.util.List;
import java.util.Map;

/**
 * REST client for Crawler API endpoints.
 * Handles starting crawls and tracking their progress.
 */
@Service
public class CrawlerApiClient {

    private final WebClient webClient;

    public CrawlerApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Start crawling - runs asynchronously in backend.
     * @deprecated Use startCrawlAsync() instead to get crawl ID
     */
    @Deprecated
    public void startCrawl(String rootUrl, Integer maxDepth, Integer maxPages) {
        System.out.println("Starting crawl: url=" + rootUrl + ", depth=" + maxDepth + ", pages=" + maxPages);

        try {
            Map<String, Object> request = Map.of(
                    "startUrl", rootUrl,
                    "maxDepth", maxDepth != null ? maxDepth : 2,
                    "maxPages", maxPages != null ? maxPages : 100
            );

            webClient.post()
                    .uri("/api/crawler/start-async")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("Crawl started successfully");

        } catch (Exception e) {
            System.err.println("Failed to start crawl: " + e.getMessage());
            throw new RuntimeException("Failed to start crawl: " + e.getMessage(), e);
        }
    }

    /**
     * Start crawling asynchronously and return response with crawl ID.
     *
     * @param rootUrl the starting URL
     * @param maxDepth maximum depth to crawl
     * @param maxPages maximum number of pages
     * @return response map containing crawlId and statusUrl
     */
    public Map<String, Object> startCrawlAsync(String rootUrl, Integer maxDepth, Integer maxPages) {
        System.out.println("Starting async crawl: url=" + rootUrl + ", depth=" + maxDepth + ", pages=" + maxPages);

        try {
            Map<String, Object> request = Map.of(
                    "startUrl", rootUrl,
                    "maxDepth", maxDepth != null ? maxDepth : 2,
                    "maxPages", maxPages != null ? maxPages : 100
            );

            Map<String, Object> response = webClient.post()
                    .uri("/api/crawler/start-async")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("Crawl started successfully with ID: " + response.get("crawlId"));
            return response;

        } catch (Exception e) {
            System.err.println("Failed to start crawl: " + e.getMessage());
            throw new RuntimeException("Failed to start crawl: " + e.getMessage(), e);
        }
    }

    /**
     * Get status of a specific crawl by ID.
     *
     * @param crawlId the crawl history ID
     * @return crawl status DTO
     */
    public CrawlHistoryDTO getCrawlStatus(Long crawlId) {
        try {
            return webClient.get()
                    .uri("/api/crawler/history/" + crawlId)
                    .retrieve()
                    .bodyToMono(CrawlHistoryDTO.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Failed to get crawl status for ID " + crawlId + ": " + e.getMessage());
            throw new RuntimeException("Failed to get crawl status: " + e.getMessage(), e);
        }
    }

    /**
     * Get all active (in-progress) crawls.
     *
     * @return list of active crawls
     */
    public List<CrawlHistoryDTO> getActiveCrawls() {
        try {
            return webClient.get()
                    .uri("/api/crawler/history?status=STARTED")
                    .retrieve()
                    .bodyToFlux(CrawlHistoryDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            System.err.println("Failed to get active crawls: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get all crawl history records.
     *
     * @return list of all crawls
     */
    public List<CrawlHistoryDTO> getAllHistory() {
        try {
            return webClient.get()
                    .uri("/api/crawler/history")
                    .retrieve()
                    .bodyToFlux(CrawlHistoryDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            System.err.println("Failed to get crawl history: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get crawl history filtered by status.
     *
     * @param status the status to filter by (SUCCESS, FAILED, PARTIAL, STARTED)
     * @return list of crawls with given status
     */
    public List<CrawlHistoryDTO> getHistoryByStatus(String status) {
        try {
            return webClient.get()
                    .uri("/api/crawler/history?status=" + status)
                    .retrieve()
                    .bodyToFlux(CrawlHistoryDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            System.err.println("Failed to get crawl history by status: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Cancel a running crawl.
     *
     * @param crawlId the ID of the crawl to cancel
     * @return true if cancelled successfully, false otherwise
     */
    public boolean cancelCrawl(Long crawlId) {
        try {
            Map<String, Object> response = webClient.post()
                    .uri("/api/crawler/cancel/" + crawlId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("Crawl cancelled successfully: " + crawlId);
            // Backend returns 200 OK with "message" if successful
            return response != null && response.containsKey("message");
        } catch (Exception e) {
            System.err.println("Failed to cancel crawl: " + e.getMessage());
            return false;
        }
    }
}
