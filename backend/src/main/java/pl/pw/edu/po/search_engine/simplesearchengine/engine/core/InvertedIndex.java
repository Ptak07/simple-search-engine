package pl.pw.edu.po.search_engine.simplesearchengine.engine.core;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe inverted index implementation.
 * Uses ConcurrentHashMap and ReadWriteLock for concurrent access.
 * Provides O(M) complexity for removeDocument where M is terms per document.
 */
public class InvertedIndex implements SearchIndex, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Mapping: term -> (document ID -> term positions)
    private final Map<String, Map<Integer, List<Integer>>> index = new ConcurrentHashMap<>();

    // Forward index: document ID -> content
    private final Map<Integer, String> forwardIndex = new ConcurrentHashMap<>();

    // Reverse mapping: document ID -> set of terms (for efficient removal)
    private final Map<Integer, Set<String>> docIdToTerms = new ConcurrentHashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private int nextDocId;

    /**
     * Add documents to the inverted index (auto-generated ID).
     *
     * @param content processed document content
     * @param tokens tokenized content
     * @return assigned document ID
     */
    @Override
    public int addDocument(String content, List<String> tokens) {
        lock.writeLock().lock();
        try {
            int docId = nextDocId++;
            addDocumentInternal(docId, content, tokens);
            return docId;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Add document with specific ID (for PostgreSQL integration).
     * Use this when you want to sync InvertedIndex with external database IDs.
     *
     * @param docId specific document ID (from PostgreSQL)
     * @param content document content
     * @param tokens tokenized content
     */
    @Override
    public void addDocument(int docId, String content, List<String> tokens) {
        lock.writeLock().lock();
        try {
            addDocumentInternal(docId, content, tokens);

            // Update nextDocId to avoid conflicts
            if (docId >= nextDocId) {
                nextDocId = docId + 1;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Internal method to add document (must be called within write lock).
     */
    private void addDocumentInternal(int docId, String content, List<String> tokens) {
        forwardIndex.put(docId, content);
        Set<String> termsInDoc = new HashSet<>();

        for (int position = 0; position < tokens.size(); position++) {
            String term = tokens.get(position);
            termsInDoc.add(term);

            index
                    .computeIfAbsent(term, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(docId, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(position);
        }

        docIdToTerms.put(docId, termsInDoc);
    }

    /**
     * Returns immutable map of documents (id -> positions) containing the term.
     * Thread-safe read operation.
     */
    @Override
    public Map<Integer, List<Integer>> getDocumentsForTerm(String term) {
        lock.readLock().lock();
        try {
            Map<Integer, List<Integer>> result = index.get(term);
            if (result == null || result.isEmpty()) {
                return Collections.emptyMap();
            }
            // Return defensive copy with unmodifiable inner lists
            Map<Integer, List<Integer>> copy = new HashMap<>();
            result.forEach((docId, positions) ->
                copy.put(docId, Collections.unmodifiableList(new ArrayList<>(positions)))
            );
            return Collections.unmodifiableMap(copy);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns original document content by its ID.
     * Thread-safe read operation.
     */
    @Override
    public String getDocumentById(int docId) {
        lock.readLock().lock();
        try {
            return forwardIndex.get(docId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns number of all documents in the index.
     * Thread-safe read operation.
     */
    @Override
    public int getDocumentCount() {
        lock.readLock().lock();
        try {
            return forwardIndex.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Print whole index (for debugging).
     */
    public void printIndex() {
        index.forEach((term, docs) -> System.out.println(term + " -> " + docs));
    }

    /**
     * Clear all documents from the index (delegation pattern support).
     * Thread-safe write operation.
     */
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            index.clear();
            forwardIndex.clear();
            docIdToTerms.clear();
            nextDocId = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Remove document from index.
     * Efficiently removes all term entries using reverse mapping.
     * Complexity: O(M) where M is number of terms in document, not O(N) of all terms.
     * Thread-safe write operation.
     */
    @Override
    public void removeDocument(int docId) {
        lock.writeLock().lock();
        try {
            // 1. Remove from forward index
            forwardIndex.remove(docId);

            // 2. Get terms for this document from reverse mapping (O(1))
            Set<String> terms = docIdToTerms.remove(docId);
            if (terms == null) {
                return; // Document not found
            }

            // 3. Remove from inverted index (O(M) where M = terms in document)
            for (String term : terms) {
                Map<Integer, List<Integer>> docMap = index.get(term);
                if (docMap != null) {
                    docMap.remove(docId);
                    // Clean up empty term entries
                    if (docMap.isEmpty()) {
                        index.remove(term);
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Merge another index into this one (delegation pattern support).
     * Preserves all documents from the new index with new document IDs.
     * Properly handles document ID remapping and maintains all data structures.
     * Thread-safe write operation.
     */
    @Override
    public void merge(SearchIndex other) {
        if (!(other instanceof InvertedIndex otherIndex)) {
            return;
        }

        lock.writeLock().lock();
        otherIndex.lock.readLock().lock();
        try {
            // Create mapping from old docId to new docId
            Map<Integer, Integer> docIdMapping = new HashMap<>();

            // 1. Merge forward index with new IDs
            otherIndex.forwardIndex.forEach((oldDocId, content) -> {
                int newDocId = nextDocId++;
                docIdMapping.put(oldDocId, newDocId);
                forwardIndex.put(newDocId, content);
            });

            // 2. Merge inverted index with remapped document IDs
            otherIndex.index.forEach((term, oldDocMap) -> {
                Map<Integer, List<Integer>> targetDocMap = index.computeIfAbsent(
                    term,
                    k -> new ConcurrentHashMap<>()
                );

                oldDocMap.forEach((oldDocId, positions) -> {
                    Integer newDocId = docIdMapping.get(oldDocId);
                    if (newDocId != null) {
                        // Create new list with same positions
                        targetDocMap.put(
                            newDocId,
                            Collections.synchronizedList(new ArrayList<>(positions))
                        );
                    }
                });
            });

            // 3. Merge reverse mapping with remapped document IDs
            otherIndex.docIdToTerms.forEach((oldDocId, terms) -> {
                Integer newDocId = docIdMapping.get(oldDocId);
                if (newDocId != null) {
                    docIdToTerms.put(newDocId, new HashSet<>(terms));
                }
            });

        } finally {
            otherIndex.lock.readLock().unlock();
            lock.writeLock().unlock();
        }
    }
}
