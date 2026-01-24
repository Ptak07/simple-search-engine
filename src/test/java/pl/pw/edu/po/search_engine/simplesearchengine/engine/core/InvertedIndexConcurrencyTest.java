package pl.pw.edu.po.search_engine.simplesearchengine.engine.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive concurrency tests for InvertedIndex.
 * Tests thread-safety with multiple readers and writers.
 * These tests verify the ReadWriteLock implementation works correctly.
 */
class InvertedIndexConcurrencyTest {

    private InvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
    }

    @RepeatedTest(5) // Run multiple times to catch race conditions
    void testConcurrentAddDocument() throws InterruptedException {
        int threadCount = 10;
        int docsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < docsPerThread; j++) {
                        List<String> tokens = List.of("thread" + threadId, "doc" + j, "test");
                        index.addDocument("Thread " + threadId + " Doc " + j, tokens);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Verify all documents were added
        assertEquals(threadCount * docsPerThread, index.getDocumentCount());
    }

    @RepeatedTest(5)
    void testConcurrentReadAndWrite() throws InterruptedException {
        // Pre-populate with some documents
        for (int i = 0; i < 50; i++) {
            index.addDocument("Initial doc " + i, List.of("initial", "doc" + i));
        }

        int writerCount = 5;
        int readerCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(writerCount + readerCount);
        CountDownLatch latch = new CountDownLatch(writerCount + readerCount);
        AtomicInteger readErrors = new AtomicInteger(0);

        // Start writers
        for (int i = 0; i < writerCount; i++) {
            final int writerId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 20; j++) {
                        index.addDocument("Writer " + writerId + " doc " + j,
                                        List.of("writer", "doc" + j));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Start readers
        for (int i = 0; i < readerCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        try {
                            Map<Integer, List<Integer>> docs = index.getDocumentsForTerm("initial");
                            assertNotNull(docs);
                            int count = index.getDocumentCount();
                            assertTrue(count >= 50); // At least initial documents
                        } catch (Exception e) {
                            readErrors.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(15, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(0, readErrors.get(), "No read errors should occur");
        assertTrue(index.getDocumentCount() >= 50); // At least initial docs
    }

    @Test
    void testConcurrentRemoveDocument() throws InterruptedException {
        // Add 100 documents
        for (int i = 0; i < 100; i++) {
            index.addDocument(i, "Document " + i, List.of("doc", "test", "word" + i));
        }

        assertEquals(100, index.getDocumentCount());

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Each thread removes 10 documents
        for (int i = 0; i < threadCount; i++) {
            final int startId = i * 10;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        index.removeDocument(startId + j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(0, index.getDocumentCount(), "All documents should be removed");
    }

    @Test
    void testConcurrentAddAndRemove() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<Integer, Boolean> addedDocs = new ConcurrentHashMap<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        if (j % 3 == 0 && !addedDocs.isEmpty()) {
                            // Remove a random document
                            Integer docId = addedDocs.keys().nextElement();
                            if (addedDocs.remove(docId) != null) {
                                index.removeDocument(docId);
                            }
                        } else {
                            // Add a document
                            int docId = index.addDocument(
                                "Thread " + threadId + " doc " + j,
                                List.of("thread" + threadId, "test")
                            );
                            addedDocs.put(docId, true);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(15, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Verify consistency
        assertEquals(addedDocs.size(), index.getDocumentCount());
    }

    @Test
    void testConcurrentMerge() throws InterruptedException {
        // Create multiple indexes to merge
        List<InvertedIndex> indexes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            InvertedIndex idx = new InvertedIndex();
            for (int j = 0; j < 20; j++) {
                idx.addDocument("Index " + i + " doc " + j, List.of("index" + i, "doc"));
            }
            indexes.add(idx);
        }

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    index.merge(indexes.get(idx));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(100, index.getDocumentCount(), "Should have 100 documents after merging 5 indexes");
    }

    @Test
    void testConcurrentClearAndAdd() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger errors = new AtomicInteger(0);

        // Thread 1: Periodically clear
        executor.submit(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(10);
                    index.clear();
                }
            } catch (InterruptedException e) {
                errors.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        // Thread 2: Continuously add
        executor.submit(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    index.addDocument("Doc " + i, List.of("test", "doc"));
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                errors.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));

        assertEquals(0, errors.get(), "No errors should occur");
        // Index count depends on timing, but should be valid
        assertTrue(index.getDocumentCount() >= 0);
    }

    @Test
    void testEncapsulationDuringConcurrentAccess() throws InterruptedException {
        index.addDocument("Test doc", List.of("test", "word"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger modificationAttempts = new AtomicInteger(0);

        // Thread 1: Try to modify returned map
        executor.submit(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    Map<Integer, List<Integer>> docs = index.getDocumentsForTerm("test");
                    try {
                        docs.put(999, List.of(1, 2, 3)); // Should fail - unmodifiable
                        modificationAttempts.incrementAndGet();
                    } catch (UnsupportedOperationException e) {
                        // Expected - map is unmodifiable
                    }
                }
            } finally {
                latch.countDown();
            }
        });

        // Thread 2: Normal operations
        executor.submit(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    index.addDocument("Doc " + i, List.of("test"));
                }
            } finally {
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(0, modificationAttempts.get(), "Should not be able to modify returned maps");
    }

    @Test
    void testHighContentionScenario() throws InterruptedException {
        int threadCount = 20;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        switch (j % 4) {
                            case 0 -> index.addDocument("Doc " + threadId + "-" + j, List.of("test"));
                            case 1 -> index.getDocumentsForTerm("test");
                            case 2 -> index.getDocumentCount();
                            case 3 -> {
                                if (j > 10) index.removeDocument(threadId * operationsPerThread + j - 10);
                            }
                        }
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(0, errors.get(), "No errors should occur under high contention");
        assertTrue(index.getDocumentCount() >= 0, "Document count should be valid");
    }
}

