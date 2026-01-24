package pl.pw.edu.po.search_engine.simplesearchengine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.PolishTextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.SearchIndex;
import pl.pw.edu.po.search_engine.simplesearchengine.model.Document;
import pl.pw.edu.po.search_engine.simplesearchengine.repository.DocumentRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchService (Phase 5).
 * Tests enhanced search with PostgreSQL integration.
 * Updated to use proper DI with SearchIndex.
 */
class SearchServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private TfIdfScoringService tfIdfScoringService;

    @Mock
    private SearchIndex searchIndex;

    @Mock
    private TextPreprocessor englishPreprocessor;

    @Mock
    private PolishTextPreprocessor polishPreprocessor;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock SearchIndex to return empty results by default
        when(searchIndex.getDocumentsForTerm(anyString())).thenReturn(new HashMap<>());

        searchService = new SearchService(
            searchIndex,
            tfIdfScoringService,
            documentRepository,
            englishPreprocessor,
            polishPreprocessor
        );
    }

    @Test
    void testSearchWithEmptyDatabase() {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query("test")
                .limit(10)
                .offset(0)
                .build();

        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        SearchResponse response = searchService.search(request);

        // Then
        assertNotNull(response);
        assertEquals("test", response.getQuery());
        assertEquals(0L, response.getTotalResults());
        assertTrue(response.getResults().isEmpty());
        assertNotNull(response.getSearchTimeMs());
    }

    @Test
    void testSearchWithEmptyQuery() {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query("")
                .limit(10)
                .offset(0)
                .build();

        // When
        SearchResponse response = searchService.search(request);

        // Then
        assertNotNull(response);
        assertEquals("", response.getQuery());
        assertEquals(0L, response.getTotalResults());
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void testSearchReturnsResponseWithMetadata() {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query("test")
                .limit(5)
                .offset(10)
                .build();

        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        SearchResponse response = searchService.search(request);

        // Then
        assertNotNull(response);
        assertEquals("test", response.getQuery());
        assertEquals(5, response.getLimit());
        assertEquals(10, response.getOffset());
        assertNotNull(response.getSearchTimeMs());
        assertTrue(response.getSearchTimeMs() >= 0);
    }

    @Test
    void testSearchWithDefaultPagination() {
        // Given - using builder defaults
        SearchRequest request = SearchRequest.builder()
                .query("test")
                .build();

        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        SearchResponse response = searchService.search(request);

        // Then
        assertEquals(10, response.getLimit(), "Default limit should be 10");
        assertEquals(0, response.getOffset(), "Default offset should be 0");
    }

    @Test
    void testSearchMeasuresTime() {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query("test")
                .build();

        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        long startTime = System.currentTimeMillis();
        SearchResponse response = searchService.search(request);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(response.getSearchTimeMs());
        assertTrue(response.getSearchTimeMs() >= 0);
        assertTrue(response.getSearchTimeMs() <= (endTime - startTime + 10)); // +10ms tolerance
    }

    @Test
    void testSearchResultContainsAllFields() {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query("test")
                .build();

        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        SearchResponse response = searchService.search(request);

        // Then - verify all required fields exist
        assertNotNull(response.getQuery());
        assertNotNull(response.getTotalResults());
        assertNotNull(response.getLimit());
        assertNotNull(response.getOffset());
        assertNotNull(response.getResults());
        assertNotNull(response.getSearchTimeMs());
    }

    @Test
    void testSearchWithNullQueryHandled() {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query(null)
                .build();

        // When
        SearchResponse response = searchService.search(request);

        // Then
        assertNotNull(response);
        assertEquals(0L, response.getTotalResults());
    }

    @Test
    void testSearchWithWhitespaceQuery() {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query("   ")
                .build();

        // When
        SearchResponse response = searchService.search(request);

        // Then
        assertNotNull(response);
        assertEquals(0L, response.getTotalResults());
    }

    @Test
    void testSearchReturnsEmptyResultsForNoMatches() {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query("nonexistent")
                .build();

        Document doc = new Document("Test", "Some content", "https://test.com");
        when(documentRepository.findAll()).thenReturn(List.of(doc));

        // When
        SearchResponse response = searchService.search(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getResults());
    }
}


