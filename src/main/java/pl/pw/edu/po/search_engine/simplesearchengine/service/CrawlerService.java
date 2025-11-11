package pl.pw.edu.po.search_engine.simplesearchengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlResult;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;

import java.io.IOException;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    private final DocumentService documentService;

    // Timout for HTTP requests (30 seconds)
    private static final int TIMEOUT_MS = 30000;

    // User-Agent - identify the crawler as a bot
    private static final String USER_AGENT = "SimpleSearchEngineBot/1.0";

    public CrawlResult crawl(CrawlRequest request) {
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
                    // Intentional delay to avoid overloading target servers (throttling)
                    Thread.sleep(request.getDelayMs());
                }

                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .get();

                pagesProcessed++;

                String title = doc.title();
                String content = doc.body().text();

                // Sprawdź czy strona ma treść
                if (content.length() > 100) {  // ← Min 100 znaków
                    // Dodaj do search engine
                    DocumentRequest docRequest = DocumentRequest.builder()
                            .title(title)
                            .content(content)
                            .url(url)
                            .build();

                    documentService.addDocument(docRequest);
                    documentsIndexed++;

                    log.info("Indexed: {} ({})", title, url);
                } else {
                    log.warn("Skipped (too short): {}", url);
                }

                if (depth < request.getMaxDepth()) {
                    Elements links = doc.select("a[href]");

                    for (Element link : links) {
                        String linkUrl = link.absUrl("href");  // ← Absolutny URL

                        // Filtruj linki
                        if (isValidUrl(linkUrl, request.getStartUrl())) {
                            urlQueue.add(new UrlWithDepth(linkUrl, depth + 1));
                        }
                    }

                    log.debug("Found {} links at depth {}", links.size(), depth);
                }

            } catch(IOException e) {
                String error = "Failed to fetch " + url + ": " + e.getMessage();
                errors.add(error);
                log.error("❌ {}", error);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        long crawlTimeMs = System.currentTimeMillis() - startTime;
        String status = errors.isEmpty() ? "SUCCESS" :
                (documentsIndexed > 0 ? "PARTIAL" : "FAILED");

        log.info("🎉 Crawl finished: {} pages, {} indexed, {} errors in {}ms",
                pagesProcessed, documentsIndexed, errors.size(), crawlTimeMs);

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
     * Validates if URL should be crawled.
     * Filters out:
     * - URLs from different domains
     * - File downloads (PDF, ZIP, JPG, etc.)
     * - Fragments (#section)
     */
    private boolean isValidUrl(String url, String startUrl) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        try {
            // Parse URLs (using URI to avoid deprecated URL constructor)
            java.net.URL urlObj = new java.net.URI(url).toURL();
            java.net.URL startUrlObj = new java.net.URI(startUrl).toURL();

            // Check if same domain (stay on same website)
            if (!urlObj.getHost().equals(startUrlObj.getHost())) {
                return false;  // Different domain
            }

            // Filter out common file extensions
            String path = urlObj.getPath().toLowerCase();
            String[] excludedExtensions = {".pdf", ".zip", ".jpg", ".jpeg", ".png", ".gif",
                                           ".doc", ".docx", ".xls", ".xlsx", ".mp3", ".mp4"};
            for (String ext : excludedExtensions) {
                if (path.endsWith(ext)) {
                    return false;  // File download
                }
            }

            // Filter out fragments (#section)
            return urlObj.getRef() == null;

        } catch (Exception e) {
            return false;  // Invalid URL
        }
    }

    /**
     * Helper record to store URL with its depth level.
     * Used for BFS (Breadth-First Search) crawling.
     */
    private record UrlWithDepth(String url, int depth) {}
}
