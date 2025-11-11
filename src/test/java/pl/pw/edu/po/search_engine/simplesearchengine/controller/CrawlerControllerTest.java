package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.CrawlResult;
import pl.pw.edu.po.search_engine.simplesearchengine.service.CrawlerService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CrawlerController.
 * Tests REST API endpoints for web crawler.
 */
@WebMvcTest(CrawlerController.class)
class CrawlerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CrawlerService crawlerService;

    @BeforeEach
    void setUp() {
        reset(crawlerService);
    }

    // ============================================
    // TEST 1: Successful crawl
    // ============================================

    @Test
    void testStartCrawlingWithSuccess() throws Exception {
        // Given
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("https://example.com")
                .maxPages(5)
                .maxDepth(2)
                .delayMs(1000L)
                .build();

        CrawlResult result = CrawlResult.builder()
                .status("SUCCESS")
                .pagesProcessed(5)
                .documentsIndexed(4)
                .errorCount(0)
                .errors(Collections.emptyList())
                .crawlTimeMs(8234L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/crawler/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.pagesProcessed").value(5))
                .andExpect(jsonPath("$.documentsIndexed").value(4))
                .andExpect(jsonPath("$.errorCount").value(0))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.crawlTimeMs").value(8234));

        verify(crawlerService, times(1)).crawl(any(CrawlRequest.class));
    }

    // ============================================
    // TEST 2: Partial success (some errors)
    // ============================================

    @Test
    void testStartCrawlingWithPartialSuccess() throws Exception {
        // Given
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("https://example.com")
                .maxPages(10)
                .maxDepth(2)
                .build();

        CrawlResult result = CrawlResult.builder()
                .status("PARTIAL")
                .pagesProcessed(8)
                .documentsIndexed(6)
                .errorCount(2)
                .errors(List.of(
                    "Timeout: https://example.com/slow",
                    "404 Not Found: https://example.com/missing"
                ))
                .crawlTimeMs(15234L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/crawler/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIAL"))
                .andExpect(jsonPath("$.pagesProcessed").value(8))
                .andExpect(jsonPath("$.documentsIndexed").value(6))
                .andExpect(jsonPath("$.errorCount").value(2))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));

        verify(crawlerService, times(1)).crawl(any(CrawlRequest.class));
    }

    // ============================================
    // TEST 3: Failed crawl
    // ============================================

    @Test
    void testStartCrawlingWithFailure() throws Exception {
        // Given
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("invalid-url")
                .maxPages(5)
                .maxDepth(2)
                .build();

        CrawlResult result = CrawlResult.builder()
                .status("FAILED")
                .pagesProcessed(0)
                .documentsIndexed(0)
                .errorCount(1)
                .errors(List.of("Invalid URL: invalid-url"))
                .crawlTimeMs(123L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/crawler/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.pagesProcessed").value(0))
                .andExpect(jsonPath("$.documentsIndexed").value(0))
                .andExpect(jsonPath("$.errorCount").value(1));
    }

    // ============================================
    // TEST 4: Request with default values
    // ============================================

    @Test
    void testStartCrawlingWithDefaultValues() throws Exception {
        // Given - minimal request (uses defaults)
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("https://example.com")
                .build();

        CrawlResult result = CrawlResult.builder()
                .status("SUCCESS")
                .pagesProcessed(3)
                .documentsIndexed(3)
                .errorCount(0)
                .errors(Collections.emptyList())
                .crawlTimeMs(5000L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/crawler/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    // ============================================
    // TEST 5: Request with all custom values
    // ============================================

    @Test
    void testStartCrawlingWithCustomValues() throws Exception {
        // Given
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("https://docs.spring.io")
                .maxPages(20)
                .maxDepth(3)
                .respectRobotsTxt(true)
                .delayMs(2000L)
                .build();

        CrawlResult result = CrawlResult.builder()
                .status("SUCCESS")
                .pagesProcessed(20)
                .documentsIndexed(18)
                .errorCount(0)
                .errors(Collections.emptyList())
                .crawlTimeMs(45000L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/crawler/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagesProcessed").value(20))
                .andExpect(jsonPath("$.documentsIndexed").value(18));
    }

    // ============================================
    // TEST 6: Response contains all required fields
    // ============================================

    @Test
    void testResponseContainsAllFields() throws Exception {
        // Given
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("https://example.com")
                .maxPages(5)
                .build();

        CrawlResult result = CrawlResult.builder()
                .status("SUCCESS")
                .pagesProcessed(5)
                .documentsIndexed(4)
                .errorCount(0)
                .errors(Collections.emptyList())
                .crawlTimeMs(8234L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/crawler/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.pagesProcessed").exists())
                .andExpect(jsonPath("$.documentsIndexed").exists())
                .andExpect(jsonPath("$.errorCount").exists())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.crawlTimeMs").exists());
    }

    // ============================================
    // TEST 7: Endpoint path is correct
    // ============================================

    @Test
    void testEndpointPath() throws Exception {
        // Given
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("https://example.com")
                .build();

        CrawlResult result = CrawlResult.builder()
                .status("SUCCESS")
                .pagesProcessed(1)
                .documentsIndexed(1)
                .errorCount(0)
                .errors(Collections.emptyList())
                .crawlTimeMs(1000L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When & Then - verify correct path works
        mockMvc.perform(post("/api/crawler/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ============================================
    // TEST 8: Returns JSON content type
    // ============================================

    @Test
    void testReturnsJsonContentType() throws Exception {
        // Given
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("https://example.com")
                .build();

        CrawlResult result = CrawlResult.builder()
                .status("SUCCESS")
                .pagesProcessed(1)
                .documentsIndexed(1)
                .errorCount(0)
                .errors(Collections.emptyList())
                .crawlTimeMs(1000L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/crawler/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ============================================
    // TEST 9: Service is called exactly once
    // ============================================

    @Test
    void testServiceCalledOnce() throws Exception {
        // Given
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("https://example.com")
                .build();

        CrawlResult result = CrawlResult.builder()
                .status("SUCCESS")
                .pagesProcessed(1)
                .documentsIndexed(1)
                .errorCount(0)
                .errors(Collections.emptyList())
                .crawlTimeMs(1000L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When
        mockMvc.perform(post("/api/crawler/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        verify(crawlerService, times(1)).crawl(any(CrawlRequest.class));
        verifyNoMoreInteractions(crawlerService);
    }

    // ============================================
    // TEST 10: Large crawl result
    // ============================================

    @Test
    void testLargeCrawlResult() throws Exception {
        // Given
        CrawlRequest request = CrawlRequest.builder()
                .startUrl("https://example.com")
                .maxPages(100)
                .build();

        // Build large error list
        List<String> errors = List.of(
            "Error 1", "Error 2", "Error 3", "Error 4", "Error 5"
        );

        CrawlResult result = CrawlResult.builder()
                .status("PARTIAL")
                .pagesProcessed(95)
                .documentsIndexed(90)
                .errorCount(5)
                .errors(errors)
                .crawlTimeMs(120000L)
                .build();

        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/crawler/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagesProcessed").value(95))
                .andExpect(jsonPath("$.documentsIndexed").value(90))
                .andExpect(jsonPath("$.errorCount").value(5))
                .andExpect(jsonPath("$.errors.length()").value(5));
    }
}

