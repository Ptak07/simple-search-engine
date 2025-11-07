package pl.pw.edu.po.search_engine.simplesearchengine.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.service.IndexingService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete search engine application.
 * Tests the full flow from indexing to searching.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchEngineIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IndexingService indexingService;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        // Clear index before each test
        indexingService.getInvertedIndex().clear();
    }

    @Test
    void testCompleteIndexingAndSearchFlow() {
        // 1. Index documents
        DocumentRequest doc1 = new DocumentRequest("1", "Machine learning is awesome");
        DocumentRequest doc2 = new DocumentRequest("2", "Deep learning is part of machine learning");
        DocumentRequest doc3 = new DocumentRequest("3", "Natural language processing");

        ResponseEntity<String> response1 = restTemplate.postForEntity(
                baseUrl + "/documents", doc1, String.class);
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());

        ResponseEntity<String> response2 = restTemplate.postForEntity(
                baseUrl + "/documents", doc2, String.class);
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());

        ResponseEntity<String> response3 = restTemplate.postForEntity(
                baseUrl + "/documents", doc3, String.class);
        assertEquals(HttpStatus.CREATED, response3.getStatusCode());

        // 2. Search for documents
        ResponseEntity<SearchResponse> searchResponse = restTemplate.getForEntity(
                baseUrl + "/search?q=machine learning", SearchResponse.class);

        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        assertNotNull(searchResponse.getBody());
        assertEquals(2, searchResponse.getBody().getResults().size());
    }

    @Test
    void testSearchReturnsResultsSortedByRelevance() {
        // Index documents with different relevance
        DocumentRequest doc1 = new DocumentRequest("1", "Java");
        DocumentRequest doc2 = new DocumentRequest("2", "Java programming");
        DocumentRequest doc3 = new DocumentRequest("3", "Java Java Java");

        restTemplate.postForEntity(baseUrl + "/documents", doc1, String.class);
        restTemplate.postForEntity(baseUrl + "/documents", doc2, String.class);
        restTemplate.postForEntity(baseUrl + "/documents", doc3, String.class);

        // Search
        ResponseEntity<SearchResponse> searchResponse = restTemplate.getForEntity(
                baseUrl + "/search?q=Java", SearchResponse.class);

        assertNotNull(searchResponse.getBody());
        var results = searchResponse.getBody().getResults();
        assertEquals(3, results.size());

        // Verify results are sorted by score
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getScore() >= results.get(i + 1).getScore());
        }
    }

    @Test
    void testSearchWithNoResults() {
        // Index a document
        DocumentRequest doc = new DocumentRequest("1", "Python programming");
        restTemplate.postForEntity(baseUrl + "/documents", doc, String.class);

        // Search for non-existent term
        ResponseEntity<SearchResponse> searchResponse = restTemplate.getForEntity(
                baseUrl + "/search?q=nonexistent", SearchResponse.class);

        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        assertNotNull(searchResponse.getBody());
        assertTrue(searchResponse.getBody().getResults().isEmpty());
    }

    @Test
    void testIndexMultipleDocumentsThenSearch() {
        // Index multiple documents
        for (int i = 0; i < 10; i++) {
            DocumentRequest doc = new DocumentRequest(
                    String.valueOf(i),
                    "Document " + i + " about programming"
            );
            restTemplate.postForEntity(baseUrl + "/documents", doc, String.class);
        }

        // Search
        ResponseEntity<SearchResponse> searchResponse = restTemplate.getForEntity(
                baseUrl + "/search?q=programming", SearchResponse.class);

        assertNotNull(searchResponse.getBody());
        assertEquals(10, searchResponse.getBody().getResults().size());
    }

    @Test
    void testSearchCaseInsensitivity() {
        DocumentRequest doc = new DocumentRequest("1", "MACHINE LEARNING");
        restTemplate.postForEntity(baseUrl + "/documents", doc, String.class);

        // Search with different cases
        ResponseEntity<SearchResponse> response1 = restTemplate.getForEntity(
                baseUrl + "/search?q=machine learning", SearchResponse.class);
        ResponseEntity<SearchResponse> response2 = restTemplate.getForEntity(
                baseUrl + "/search?q=MACHINE LEARNING", SearchResponse.class);

        assertNotNull(response1.getBody());
        assertNotNull(response2.getBody());
        assertEquals(1, response1.getBody().getResults().size());
        assertEquals(1, response2.getBody().getResults().size());
    }

    @Test
    void testSearchWithStopWords() {
        DocumentRequest doc = new DocumentRequest("1", "The quick brown fox");
        restTemplate.postForEntity(baseUrl + "/documents", doc, String.class);

        // Search with stop words
        ResponseEntity<SearchResponse> searchResponse = restTemplate.getForEntity(
                baseUrl + "/search?q=quick brown", SearchResponse.class);

        assertNotNull(searchResponse.getBody());
        assertFalse(searchResponse.getBody().getResults().isEmpty());
    }

    @Test
    void testAddDocumentWithEmptyContent() {
        DocumentRequest doc = new DocumentRequest("1", "");
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/documents", doc, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testPrintIndexEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/index", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Index printed to console"));
    }

    @Test
    void testSearchPreservesOriginalContent() {
        String originalContent = "The Quick Brown Fox Jumps";
        DocumentRequest doc = new DocumentRequest("1", originalContent);
        restTemplate.postForEntity(baseUrl + "/documents", doc, String.class);

        ResponseEntity<SearchResponse> searchResponse = restTemplate.getForEntity(
                baseUrl + "/search?q=fox", SearchResponse.class);

        assertNotNull(searchResponse.getBody());
        assertEquals(1, searchResponse.getBody().getResults().size());
        assertEquals(originalContent, searchResponse.getBody().getResults().get(0).getContent());
    }

    @Test
    void testSearchWithSpecialCharacters() {
        DocumentRequest doc = new DocumentRequest("1", "Hello, World! How are you?");
        restTemplate.postForEntity(baseUrl + "/documents", doc, String.class);

        ResponseEntity<SearchResponse> searchResponse = restTemplate.getForEntity(
                baseUrl + "/search?q=hello world", SearchResponse.class);

        assertNotNull(searchResponse.getBody());
        assertEquals(1, searchResponse.getBody().getResults().size());
    }

    @Test
    void testMultipleSearchesReturnConsistentResults() {
        DocumentRequest doc = new DocumentRequest("1", "Consistent search results");
        restTemplate.postForEntity(baseUrl + "/documents", doc, String.class);

        // Perform same search multiple times
        ResponseEntity<SearchResponse> response1 = restTemplate.getForEntity(
                baseUrl + "/search?q=consistent", SearchResponse.class);
        ResponseEntity<SearchResponse> response2 = restTemplate.getForEntity(
                baseUrl + "/search?q=consistent", SearchResponse.class);

        assertNotNull(response1.getBody());
        assertNotNull(response2.getBody());
        assertEquals(response1.getBody().getResults().size(),
                    response2.getBody().getResults().size());

        if (!response1.getBody().getResults().isEmpty()) {
            assertEquals(response1.getBody().getResults().get(0).getScore(),
                        response2.getBody().getResults().get(0).getScore(), 0.0001);
        }
    }

    @Test
    void testSearchAfterIndexingMultipleTimes() {
        // Index first document
        DocumentRequest doc1 = new DocumentRequest("1", "First document");
        restTemplate.postForEntity(baseUrl + "/documents", doc1, String.class);

        ResponseEntity<SearchResponse> search1 = restTemplate.getForEntity(
                baseUrl + "/search?q=document", SearchResponse.class);
        assertNotNull(search1.getBody());
        assertEquals(1, search1.getBody().getResults().size());

        // Index second document
        DocumentRequest doc2 = new DocumentRequest("2", "Second document");
        restTemplate.postForEntity(baseUrl + "/documents", doc2, String.class);

        ResponseEntity<SearchResponse> search2 = restTemplate.getForEntity(
                baseUrl + "/search?q=document", SearchResponse.class);
        assertNotNull(search2.getBody());
        assertEquals(2, search2.getBody().getResults().size());
    }

    @Test
    void testLargeScaleIndexingAndSearch() {
        // Index many documents
        for (int i = 0; i < 50; i++) {
            DocumentRequest doc = new DocumentRequest(
                    String.valueOf(i),
                    "Document " + i + " with content about topic " + (i % 5)
            );
            restTemplate.postForEntity(baseUrl + "/documents", doc, String.class);
        }

        // Search should work efficiently
        ResponseEntity<SearchResponse> searchResponse = restTemplate.getForEntity(
                baseUrl + "/search?q=document content", SearchResponse.class);

        assertNotNull(searchResponse.getBody());
        assertFalse(searchResponse.getBody().getResults().isEmpty());
    }

    @Test
    void testSearchWithMultipleTerms() {
        DocumentRequest doc1 = new DocumentRequest("1", "Machine learning algorithms");
        DocumentRequest doc2 = new DocumentRequest("2", "Machine learning and deep learning");
        DocumentRequest doc3 = new DocumentRequest("3", "Algorithms for sorting");

        restTemplate.postForEntity(baseUrl + "/documents", doc1, String.class);
        restTemplate.postForEntity(baseUrl + "/documents", doc2, String.class);
        restTemplate.postForEntity(baseUrl + "/documents", doc3, String.class);

        // Search for multiple terms
        ResponseEntity<SearchResponse> searchResponse = restTemplate.getForEntity(
                baseUrl + "/search?q=machine algorithms", SearchResponse.class);

        assertNotNull(searchResponse.getBody());
        // Should return documents containing both terms
        assertFalse(searchResponse.getBody().getResults().isEmpty());
    }
}

