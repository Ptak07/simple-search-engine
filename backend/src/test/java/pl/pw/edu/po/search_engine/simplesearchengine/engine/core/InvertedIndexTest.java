package pl.pw.edu.po.search_engine.simplesearchengine.engine.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvertedIndex.
 * Tests document addition, retrieval, and index operations.
 */
class InvertedIndexTest {

    private InvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
    }

    @Test
    void testAddSingleDocument() {
        List<String> tokens = List.of("hello", "world");
        int docId = index.addDocument("Hello World", tokens);

        assertEquals(0, docId, "First document should have ID 0");
        assertEquals(1, index.getDocumentCount());
        assertEquals("Hello World", index.getDocumentById(docId));
    }

    @Test
    void testAddMultipleDocuments() {
        int docId1 = index.addDocument("First document", List.of("first", "document"));
        int docId2 = index.addDocument("Second document", List.of("second", "document"));
        int docId3 = index.addDocument("Third document", List.of("third", "document"));

        assertEquals(0, docId1);
        assertEquals(1, docId2);
        assertEquals(2, docId3);
        assertEquals(3, index.getDocumentCount());
    }

    @Test
    void testGetDocumentsForTerm() {
        index.addDocument("Hello World", List.of("hello", "world"));
        index.addDocument("Hello Java", List.of("hello", "java"));

        Map<Integer, List<Integer>> helloPostings = index.getDocumentsForTerm("hello");
        assertEquals(2, helloPostings.size(), "Term 'hello' should appear in 2 documents");

        Map<Integer, List<Integer>> worldPostings = index.getDocumentsForTerm("world");
        assertEquals(1, worldPostings.size(), "Term 'world' should appear in 1 document");
    }

    @Test
    void testGetDocumentsForNonExistentTerm() {
        index.addDocument("Hello World", List.of("hello", "world"));

        Map<Integer, List<Integer>> postings = index.getDocumentsForTerm("nonexistent");
        assertTrue(postings.isEmpty(), "Non-existent term should return empty map");
    }

    @Test
    void testTermPositions() {
        List<String> tokens = List.of("hello", "world", "hello", "java");
        int docId = index.addDocument("test content", tokens);

        Map<Integer, List<Integer>> helloPostings = index.getDocumentsForTerm("hello");
        List<Integer> positions = helloPostings.get(docId);

        assertNotNull(positions);
        assertEquals(2, positions.size(), "Term 'hello' appears twice");
        assertTrue(positions.contains(0), "First occurrence at position 0");
        assertTrue(positions.contains(2), "Second occurrence at position 2");
    }

    @Test
    void testGetDocumentById() {
        int docId = index.addDocument("Test content", List.of("test", "content"));

        String content = index.getDocumentById(docId);
        assertEquals("Test content", content);
    }

    @Test
    void testGetDocumentByIdNonExistent() {
        String content = index.getDocumentById(999);
        assertNull(content, "Non-existent document ID should return null");
    }

    @Test
    void testEmptyIndex() {
        assertEquals(0, index.getDocumentCount());
        assertTrue(index.getDocumentsForTerm("any").isEmpty());
    }

    @Test
    void testAddDocumentWithEmptyTokens() {
        int docId = index.addDocument("Empty tokens doc", List.of());

        assertEquals(0, docId);
        assertEquals(1, index.getDocumentCount());
        assertEquals("Empty tokens doc", index.getDocumentById(docId));
    }

    @Test
    void testAddDocumentWithDuplicateTerms() {
        List<String> tokens = List.of("test", "test", "test");
        int docId = index.addDocument("Test test test", tokens);

        Map<Integer, List<Integer>> postings = index.getDocumentsForTerm("test");
        List<Integer> positions = postings.get(docId);

        assertEquals(3, positions.size(), "Should record all occurrences");
        assertEquals(List.of(0, 1, 2), positions, "Positions should be 0, 1, 2");
    }

    @Test
    void testClear() {
        index.addDocument("Doc 1", List.of("hello"));
        index.addDocument("Doc 2", List.of("world"));

        assertEquals(2, index.getDocumentCount());

        index.clear();

        assertEquals(0, index.getDocumentCount());
        assertTrue(index.getDocumentsForTerm("hello").isEmpty());
        assertTrue(index.getDocumentsForTerm("world").isEmpty());
    }

    @Test
    void testMerge() {
        // Create first index
        index.addDocument("Doc 1", List.of("hello", "world"));
        index.addDocument("Doc 2", List.of("java", "spring"));

        // Create second index
        InvertedIndex otherIndex = new InvertedIndex();
        otherIndex.addDocument("Doc 3", List.of("python", "django"));
        otherIndex.addDocument("Doc 4", List.of("javascript", "react"));

        int originalCount = index.getDocumentCount();
        index.merge(otherIndex);

        assertEquals(originalCount + otherIndex.getDocumentCount(), index.getDocumentCount());
    }

    @Test
    void testMergeWithNull() {
        index.addDocument("Doc 1", List.of("hello"));
        int originalCount = index.getDocumentCount();

        index.merge(null);

        assertEquals(originalCount, index.getDocumentCount(), "Merging null should not change index");
    }

    @Test
    void testMergeEmptyIndex() {
        index.addDocument("Doc 1", List.of("hello"));

        InvertedIndex emptyIndex = new InvertedIndex();
        int originalCount = index.getDocumentCount();

        index.merge(emptyIndex);

        assertEquals(originalCount, index.getDocumentCount());
    }

    @Test
    void testClearAndReuse() {
        index.addDocument("Doc 1", List.of("hello"));
        index.clear();

        int newDocId = index.addDocument("Doc 2", List.of("world"));

        assertEquals(0, newDocId, "After clear, document IDs should start from 0");
        assertEquals(1, index.getDocumentCount());
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        // Basic concurrency test
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                index.addDocument("Thread 1 doc " + i, List.of("thread", "one"));
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                index.addDocument("Thread 2 doc " + i, List.of("thread", "two"));
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(20, index.getDocumentCount(), "Should have 20 documents total");
        Map<Integer, List<Integer>> threadPostings = index.getDocumentsForTerm("thread");
        assertEquals(20, threadPostings.size(), "Term 'thread' should appear in all 20 documents");
    }

    @Test
    void testMultipleTermsPerDocument() {
        List<String> tokens = List.of("machine", "learning", "artificial", "intelligence");
        int docId = index.addDocument("ML and AI", tokens);

        assertEquals(4, tokens.size());

        // Check each term is indexed
        assertFalse(index.getDocumentsForTerm("machine").isEmpty());
        assertFalse(index.getDocumentsForTerm("learning").isEmpty());
        assertFalse(index.getDocumentsForTerm("artificial").isEmpty());
        assertFalse(index.getDocumentsForTerm("intelligence").isEmpty());

        // All should point to the same document
        assertEquals(docId, index.getDocumentsForTerm("machine").keySet().iterator().next());
        assertEquals(docId, index.getDocumentsForTerm("learning").keySet().iterator().next());
    }
}

