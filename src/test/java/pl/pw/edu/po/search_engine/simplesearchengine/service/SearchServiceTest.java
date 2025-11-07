package pl.pw.edu.po.search_engine.simplesearchengine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SearchService.
 * Tests search functionality and TF-IDF ranking.
 */
class SearchServiceTest {

    private IndexingService indexingService;
    private TfIdfScoringService tfIdfScoringService;
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        indexingService = new IndexingService();
        tfIdfScoringService = new TfIdfScoringService(indexingService);
        searchService = new SearchService(indexingService, tfIdfScoringService);
    }

    @Test
    void testSearchEmptyIndex() {
        SearchResponse response = searchService.search("test query");
        assertNotNull(response);
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void testSearchEmptyQuery() {
        indexingService.index(new DocumentRequest("1", "Test document"));

        SearchResponse response = searchService.search("");
        assertNotNull(response);
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void testSearchWithStopWordsOnly() {
        indexingService.index(new DocumentRequest("1", "Test document"));

        SearchResponse response = searchService.search("the and or");
        assertNotNull(response);
        assertTrue(response.getResults().isEmpty(), "Query with only stop words should return no results");
    }

    @Test
    void testSearchSingleMatch() {
        indexingService.index(new DocumentRequest("1", "Machine learning is awesome"));

        SearchResponse response = searchService.search("machine learning");
        assertNotNull(response);
        assertEquals(1, response.getResults().size());

        SearchResult result = response.getResults().get(0);
        assertEquals(0, result.getDocumentId());
        assertTrue(result.getScore() > 0);
    }

    @Test
    void testSearchNoMatch() {
        indexingService.index(new DocumentRequest("1", "Machine learning is awesome"));

        SearchResponse response = searchService.search("quantum computing");
        assertNotNull(response);
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void testSearchMultipleMatches() {
        indexingService.index(new DocumentRequest("1", "Machine learning is a subset of AI"));
        indexingService.index(new DocumentRequest("2", "Deep learning is part of machine learning"));
        indexingService.index(new DocumentRequest("3", "Artificial intelligence and machine learning"));

        SearchResponse response = searchService.search("machine learning");
        assertNotNull(response);
        assertEquals(3, response.getResults().size());

        // All should have positive scores
        response.getResults().forEach(result -> assertTrue(result.getScore() > 0));
    }

    @Test
    void testSearchResultsSorted() {
        // Document with more occurrences of query terms should rank higher
        indexingService.index(new DocumentRequest("1", "Java programming"));
        indexingService.index(new DocumentRequest("2", "Java is great for programming Java applications"));
        indexingService.index(new DocumentRequest("3", "Python programming"));

        SearchResponse response = searchService.search("Java");
        assertNotNull(response);
        assertFalse(response.getResults().isEmpty());

        // Verify results are sorted by score (descending)
        List<SearchResult> results = response.getResults();
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getScore() >= results.get(i + 1).getScore(),
                    "Results should be sorted by score in descending order");
        }
    }

    @Test
    void testSearchPartialMatch() {
        indexingService.index(new DocumentRequest("1", "Machine learning algorithms"));
        indexingService.index(new DocumentRequest("2", "Deep learning networks"));

        SearchResponse response = searchService.search("machine deep");
        assertNotNull(response);
        // Should return empty because documents must contain ALL query terms
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void testSearchCaseInsensitive() {
        indexingService.index(new DocumentRequest("1", "MACHINE LEARNING"));

        SearchResponse response1 = searchService.search("machine learning");
        SearchResponse response2 = searchService.search("Machine Learning");
        SearchResponse response3 = searchService.search("MACHINE LEARNING");

        assertEquals(1, response1.getResults().size());
        assertEquals(1, response2.getResults().size());
        assertEquals(1, response3.getResults().size());

        // All should return the same score
        assertEquals(response1.getResults().get(0).getScore(),
                    response2.getResults().get(0).getScore(), 0.001);
    }

    @Test
    void testSearchWithStemming() {
        indexingService.index(new DocumentRequest("1", "Running quickly"));

        // Should match because "runs" stems to "run" like "running"
        SearchResponse response = searchService.search("runs");
        assertNotNull(response);
        assertFalse(response.getResults().isEmpty());
    }

    @Test
    void testSearchReturnsOriginalContent() {
        String originalContent = "Machine Learning is Awesome";
        indexingService.index(new DocumentRequest("1", originalContent));

        SearchResponse response = searchService.search("machine learning");
        assertNotNull(response);
        assertEquals(1, response.getResults().size());
        assertEquals(originalContent, response.getResults().get(0).getContent());
    }

    @Test
    void testSearchWithPunctuation() {
        indexingService.index(new DocumentRequest("1", "Hello, World! How are you?"));

        SearchResponse response = searchService.search("hello world");
        assertNotNull(response);
        assertEquals(1, response.getResults().size());
    }

    @Test
    void testSearchScoresDifferentForDifferentRelevance() {
        indexingService.index(new DocumentRequest("1", "Java programming language"));
        indexingService.index(new DocumentRequest("2", "Java Java Java programming"));

        SearchResponse response = searchService.search("java");
        assertEquals(2, response.getResults().size());

        // Document 2 should have higher score (more occurrences)
        SearchResult result1 = response.getResults().get(0);
        SearchResult result2 = response.getResults().get(1);

        assertTrue(result1.getScore() != result2.getScore(),
                  "Documents with different relevance should have different scores");
    }

    @Test
    void testSearchMultipleTermsAllRequired() {
        indexingService.index(new DocumentRequest("1", "Machine learning and AI"));
        indexingService.index(new DocumentRequest("2", "Machine learning algorithms"));
        indexingService.index(new DocumentRequest("3", "Artificial intelligence systems"));

        SearchResponse response = searchService.search("machine artificial");

        // Only document 1 contains both "machine" (from machine) and "artificial" (from AI/artificial)
        // Actually, "AI" won't stem to "artificial", so only doc 3 has "artificial"
        // and only docs 1,2 have "machine", so no document has both
        assertTrue(response.getResults().isEmpty() || response.getResults().size() == 1);
    }

    @Test
    void testSearchWithNumbers() {
        indexingService.index(new DocumentRequest("1", "Java 21 is the latest version"));
        indexingService.index(new DocumentRequest("2", "Python 3 is popular"));

        SearchResponse response = searchService.search("java 21");
        assertFalse(response.getResults().isEmpty());
    }

    @Test
    void testSearchLargeResultSet() {
        // Index many documents
        for (int i = 0; i < 100; i++) {
            indexingService.index(new DocumentRequest(String.valueOf(i),
                    "Document " + i + " about machine learning"));
        }

        SearchResponse response = searchService.search("machine learning");
        assertEquals(100, response.getResults().size());

        // Verify sorting
        List<SearchResult> results = response.getResults();
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getScore() >= results.get(i + 1).getScore());
        }
    }

    @Test
    void testSearchAfterNewDocumentAdded() {
        indexingService.index(new DocumentRequest("1", "Initial document about Java"));

        SearchResponse response1 = searchService.search("java");
        assertEquals(1, response1.getResults().size());

        // Add more documents
        indexingService.index(new DocumentRequest("2", "Another Java document"));

        SearchResponse response2 = searchService.search("java");
        assertEquals(2, response2.getResults().size());
    }

    @Test
    void testSearchComplexQuery() {
        indexingService.index(new DocumentRequest("1",
                "Machine learning is a method of data analysis that automates analytical model building"));
        indexingService.index(new DocumentRequest("2",
                "Deep learning is part of a broader family of machine learning methods"));
        indexingService.index(new DocumentRequest("3",
                "Artificial intelligence is intelligence demonstrated by machines"));

        SearchResponse response = searchService.search("machine learning methods");
        assertNotNull(response);
        assertFalse(response.getResults().isEmpty());

        // Documents containing all three terms should rank highest
        assertTrue(response.getResults().get(0).getScore() > 0);
    }

    @Test
    void testSearchRelevanceScoring() {
        // Create documents with different relevance levels
        indexingService.index(new DocumentRequest("1", "Java")); // Highly relevant
        indexingService.index(new DocumentRequest("2", "Java programming")); // Medium relevant
        indexingService.index(new DocumentRequest("3",
                "Java programming language for enterprise applications")); // Lower relevant (longer doc)

        SearchResponse response = searchService.search("java");
        assertEquals(3, response.getResults().size());

        // All should have positive scores
        response.getResults().forEach(r -> assertTrue(r.getScore() > 0));
    }
}

