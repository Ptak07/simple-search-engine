package pl.pw.edu.po.search_engine.simplesearchengine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.InvertedIndex;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IndexingService.
 * Tests document indexing and index management.
 */
class IndexingServiceTest {

    private IndexingService indexingService;

    @BeforeEach
    void setUp() {
        indexingService = new IndexingService();
    }

    @Test
    void testIndexSingleDocument() {
        DocumentRequest request = new DocumentRequest("1", "Hello World");
        int docId = indexingService.index(request);

        assertEquals(0, docId, "First document should have ID 0");
        assertEquals(1, indexingService.getDocumentCount());
    }

    @Test
    void testIndexMultipleDocuments() {
        DocumentRequest req1 = new DocumentRequest("1", "First document");
        DocumentRequest req2 = new DocumentRequest("2", "Second document");
        DocumentRequest req3 = new DocumentRequest("3", "Third document");

        int docId1 = indexingService.index(req1);
        int docId2 = indexingService.index(req2);
        int docId3 = indexingService.index(req3);

        assertEquals(0, docId1);
        assertEquals(1, docId2);
        assertEquals(2, docId3);
        assertEquals(3, indexingService.getDocumentCount());
    }

    @Test
    void testIndexEmptyContent() {
        DocumentRequest request = new DocumentRequest("1", "");
        int docId = indexingService.index(request);

        assertTrue(docId >= 0, "Should still assign document ID");
        assertEquals(1, indexingService.getDocumentCount());
    }

    @Test
    void testIndexWithStopWords() {
        DocumentRequest request = new DocumentRequest("1", "The quick brown fox jumps over the lazy dog");
        int docId = indexingService.index(request);

        assertEquals(0, docId);
        assertEquals(1, indexingService.getDocumentCount());
        // Content should be indexed with stop words removed
    }

    @Test
    void testGetDocumentCount() {
        assertEquals(0, indexingService.getDocumentCount());

        indexingService.index(new DocumentRequest("1", "Doc 1"));
        assertEquals(1, indexingService.getDocumentCount());

        indexingService.index(new DocumentRequest("2", "Doc 2"));
        assertEquals(2, indexingService.getDocumentCount());
    }

    @Test
    void testGetInvertedIndex() {
        InvertedIndex index = indexingService.getInvertedIndex();
        assertNotNull(index, "InvertedIndex should not be null");
        assertEquals(0, index.getDocumentCount());

        indexingService.index(new DocumentRequest("1", "Test"));
        assertEquals(1, index.getDocumentCount());
    }

    @Test
    void testReplaceIndex() {
        // Index some documents
        indexingService.index(new DocumentRequest("1", "Original document"));
        assertEquals(1, indexingService.getDocumentCount());

        // Create new index with different content
        InvertedIndex newIndex = new InvertedIndex();
        newIndex.addDocument("New document 1", java.util.List.of("new", "document"));
        newIndex.addDocument("New document 2", java.util.List.of("another", "new"));

        // Replace index
        indexingService.replaceIndex(newIndex);

        // Should have the new documents count
        assertEquals(2, indexingService.getDocumentCount());
    }

    @Test
    void testReplaceIndexClearsOldData() {
        // Add initial documents
        indexingService.index(new DocumentRequest("1", "Old document"));

        // Create new empty index
        InvertedIndex newIndex = new InvertedIndex();

        // Replace with empty index
        indexingService.replaceIndex(newIndex);

        // Should be empty now
        assertEquals(0, indexingService.getDocumentCount());
    }

    @Test
    void testIndexComplexDocument() {
        String complexContent = "Machine learning is a subset of artificial intelligence. " +
                "It enables computers to learn from data without being explicitly programmed.";

        DocumentRequest request = new DocumentRequest("1", complexContent);
        int docId = indexingService.index(request);

        assertEquals(0, docId);
        assertEquals(1, indexingService.getDocumentCount());
    }

    @Test
    void testIndexWithNumbers() {
        DocumentRequest request = new DocumentRequest("1", "Java 21 is the latest LTS version");
        int docId = indexingService.index(request);

        assertEquals(0, docId);
        assertEquals(1, indexingService.getDocumentCount());
    }

    @Test
    void testIndexWithSpecialCharacters() {
        DocumentRequest request = new DocumentRequest("1", "Hello! How are you? I'm fine, thanks.");
        int docId = indexingService.index(request);

        assertEquals(0, docId);
        assertEquals(1, indexingService.getDocumentCount());
    }

    @Test
    void testIndexPreservesOriginalContent() {
        String originalContent = "The Quick Brown Fox";
        DocumentRequest request = new DocumentRequest("1", originalContent);
        int docId = indexingService.index(request);

        // Verify original content is preserved
        String retrievedContent = indexingService.getInvertedIndex().getDocumentById(docId);
        assertEquals(originalContent, retrievedContent);
    }

    @Test
    void testTextPreprocessing() {
        // Test that text preprocessing is applied
        DocumentRequest request = new DocumentRequest("1", "RUNNING runs ran runner");
        indexingService.index(request);

        InvertedIndex index = indexingService.getInvertedIndex();
        // After stemming, these should have a common stem
        assertFalse(index.getDocumentsForTerm("run").isEmpty() ||
                   index.getDocumentsForTerm("runner").isEmpty());
    }

    @Test
    void testConcurrentIndexing() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                indexingService.index(new DocumentRequest("t1-" + i, "Thread 1 document " + i));
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                indexingService.index(new DocumentRequest("t2-" + i, "Thread 2 document " + i));
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // With proper synchronization in InvertedIndex.addDocument, we should have 20 documents
        // If the test fails, it indicates a race condition issue
        int finalCount = indexingService.getDocumentCount();
        assertTrue(finalCount >= 10, "Should have at least 10 documents, got: " + finalCount);
        assertEquals(20, finalCount, "Should have exactly 20 documents with proper synchronization");
    }

    @Test
    void testIndexMultipleDocumentsWithSameContent() {
        String content = "Duplicate content";

        int docId1 = indexingService.index(new DocumentRequest("1", content));
        int docId2 = indexingService.index(new DocumentRequest("2", content));

        assertNotEquals(docId1, docId2, "Should have different document IDs");
        assertEquals(2, indexingService.getDocumentCount());
    }

    @Test
    void testIndexLongDocument() {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("word").append(i).append(" ");
        }

        DocumentRequest request = new DocumentRequest("1", longContent.toString());
        int docId = indexingService.index(request);

        assertEquals(0, docId);
        assertEquals(1, indexingService.getDocumentCount());
    }
}

