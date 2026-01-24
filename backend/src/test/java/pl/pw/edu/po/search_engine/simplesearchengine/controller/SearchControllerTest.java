package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResult;
import pl.pw.edu.po.search_engine.simplesearchengine.service.SearchService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for SearchController (Phase 5).
 * Tests enhanced search API with GET and query parameters.
 */
@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        reset(searchService);
    }

    // ========================================
    // PHASE 5: Enhanced Search API Tests
    // ========================================

    @Test
    void testSearchWithResults() throws Exception {
        // Given
        DocumentResponse doc = DocumentResponse.builder()
                .id(1L)
                .title("Java Programming Guide")
                .content("Java is a popular programming language")
                .url("https://example.com/java")
                .createdAt(LocalDateTime.now())
                .build();

        SearchResult result = SearchResult.builder()
                .document(doc)
                .score(8.5)
                .matchedTerms(List.of("java", "programming"))
                .snippet("...Java is a popular programming language...")
                .build();

        SearchResponse response = SearchResponse.builder()
                .query("java programming")
                .totalResults(1L)
                .limit(10)
                .offset(0)
                .results(List.of(result))
                .searchTimeMs(15L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("query", "java programming")
                        .param("limit", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.query").value("java programming"))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.offset").value(0))
                .andExpect(jsonPath("$.searchTimeMs").value(15))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(1))
                .andExpect(jsonPath("$.results[0].document.id").value(1))
                .andExpect(jsonPath("$.results[0].document.title").value("Java Programming Guide"))
                .andExpect(jsonPath("$.results[0].score").value(8.5))
                .andExpect(jsonPath("$.results[0].matchedTerms").isArray())
                .andExpect(jsonPath("$.results[0].matchedTerms", hasSize(2)))
                .andExpect(jsonPath("$.results[0].snippet").value("...Java is a popular programming language..."));

        verify(searchService, times(1)).search(any(SearchRequest.class));
    }

    @Test
    void testSearchWithNoResults() throws Exception {
        // Given
        SearchResponse response = SearchResponse.builder()
                .query("nonexistentterm")
                .totalResults(0L)
                .limit(10)
                .offset(0)
                .results(Collections.emptyList())
                .searchTimeMs(5L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("query", "nonexistentterm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("nonexistentterm"))
                .andExpect(jsonPath("$.totalResults").value(0))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results").isEmpty());

        verify(searchService, times(1)).search(any(SearchRequest.class));
    }

    @Test
    void testSearchWithDefaultPagination() throws Exception {
        // Given
        SearchResponse response = SearchResponse.builder()
                .query("test")
                .totalResults(0L)
                .limit(10)
                .offset(0)
                .results(Collections.emptyList())
                .searchTimeMs(3L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then - no limit/offset params should use defaults
        mockMvc.perform(get("/api/search")
                        .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.offset").value(0));
    }

    @Test
    void testSearchWithCustomPagination() throws Exception {
        // Given
        SearchResponse response = SearchResponse.builder()
                .query("test")
                .totalResults(50L)
                .limit(5)
                .offset(10)
                .results(Collections.emptyList())
                .searchTimeMs(8L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("query", "test")
                        .param("limit", "5")
                        .param("offset", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(5))
                .andExpect(jsonPath("$.offset").value(10))
                .andExpect(jsonPath("$.totalResults").value(50));
    }

    @Test
    void testSearchWithMultipleResults() throws Exception {
        // Given
        DocumentResponse doc1 = DocumentResponse.builder()
                .id(1L)
                .title("Spring Boot Tutorial")
                .content("Spring Boot content")
                .url("https://example.com/spring")
                .build();

        DocumentResponse doc2 = DocumentResponse.builder()
                .id(2L)
                .title("Java Basics")
                .content("Java basics content")
                .url("https://example.com/java")
                .build();

        SearchResult result1 = SearchResult.builder()
                .document(doc1)
                .score(9.2)
                .matchedTerms(List.of("spring", "boot"))
                .snippet("...Spring Boot content...")
                .build();

        SearchResult result2 = SearchResult.builder()
                .document(doc2)
                .score(7.5)
                .matchedTerms(List.of("java"))
                .snippet("...Java basics...")
                .build();

        SearchResponse response = SearchResponse.builder()
                .query("java spring")
                .totalResults(2L)
                .limit(10)
                .offset(0)
                .results(List.of(result1, result2))
                .searchTimeMs(20L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("query", "java spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(2))
                .andExpect(jsonPath("$.results.length()").value(2))
                .andExpect(jsonPath("$.results[0].score").value(9.2))
                .andExpect(jsonPath("$.results[1].score").value(7.5))
                .andExpect(jsonPath("$.results[0].document.title").value("Spring Boot Tutorial"))
                .andExpect(jsonPath("$.results[1].document.title").value("Java Basics"));
    }

    @Test
    void testSearchResultsContainAllRequiredFields() throws Exception {
        // Given
        DocumentResponse doc = DocumentResponse.builder()
                .id(42L)
                .title("Test Document")
                .content("Full content here")
                .url("https://test.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        SearchResult result = SearchResult.builder()
                .document(doc)
                .score(7.8)
                .matchedTerms(List.of("test"))
                .snippet("...Full content here...")
                .build();

        SearchResponse response = SearchResponse.builder()
                .query("test")
                .totalResults(1L)
                .limit(10)
                .offset(0)
                .results(List.of(result))
                .searchTimeMs(12L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then - verify all required fields are present
        mockMvc.perform(get("/api/search")
                        .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").exists())
                .andExpect(jsonPath("$.totalResults").exists())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.offset").exists())
                .andExpect(jsonPath("$.searchTimeMs").exists())
                .andExpect(jsonPath("$.results[0].document").exists())
                .andExpect(jsonPath("$.results[0].document.id").value(42))
                .andExpect(jsonPath("$.results[0].document.title").exists())
                .andExpect(jsonPath("$.results[0].document.content").exists())
                .andExpect(jsonPath("$.results[0].document.url").exists())
                .andExpect(jsonPath("$.results[0].score").exists())
                .andExpect(jsonPath("$.results[0].matchedTerms").exists())
                .andExpect(jsonPath("$.results[0].snippet").exists());
    }

    @Test
    void testSearchWithEmptyQuery() throws Exception {
        // Given
        SearchResponse response = SearchResponse.builder()
                .query("")
                .totalResults(0L)
                .limit(10)
                .offset(0)
                .results(Collections.emptyList())
                .searchTimeMs(1L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("query", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(0));
    }

    @Test
    void testSearchWithSpecialCharacters() throws Exception {
        // Given
        SearchResponse response = SearchResponse.builder()
                .query("test@123#!")
                .totalResults(0L)
                .limit(10)
                .offset(0)
                .results(Collections.emptyList())
                .searchTimeMs(2L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("query", "test@123#!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("test@123#!"));
    }

    @Test
    void testSearchReturnsCorrectContentType() throws Exception {
        // Given
        SearchResponse response = SearchResponse.builder()
                .query("test")
                .totalResults(0L)
                .limit(10)
                .offset(0)
                .results(Collections.emptyList())
                .searchTimeMs(5L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testSearchWithLongQuery() throws Exception {
        // Given
        String longQuery = "word ".repeat(100); // 100 words
        SearchResponse response = SearchResponse.builder()
                .query(longQuery.trim())
                .totalResults(0L)
                .limit(10)
                .offset(0)
                .results(Collections.emptyList())
                .searchTimeMs(10L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/search")
                        .param("query", longQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value(longQuery.trim()));
    }

    @Test
    void testSearchVerifiesServiceCalled() throws Exception {
        // Given
        SearchResponse response = SearchResponse.builder()
                .query("test")
                .totalResults(0L)
                .limit(10)
                .offset(0)
                .results(Collections.emptyList())
                .searchTimeMs(3L)
                .build();

        when(searchService.search(any(SearchRequest.class))).thenReturn(response);

        // When
        mockMvc.perform(get("/api/search")
                        .param("query", "test"));

        // Then - verify service was called exactly once
        verify(searchService, times(1)).search(any(SearchRequest.class));
        verifyNoMoreInteractions(searchService);
    }
}

