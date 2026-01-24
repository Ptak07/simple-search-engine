package pl.pw.edu.po.search_engine.frontend.services.api;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * REST client for Crawler API endpoints.
 * Simplified - just starts crawl, no tracking.
 */
@Service
public class CrawlerApiClient {

    private final WebClient webClient;

    public CrawlerApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Start crawling - runs asynchronously in backend.
     */
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
}
