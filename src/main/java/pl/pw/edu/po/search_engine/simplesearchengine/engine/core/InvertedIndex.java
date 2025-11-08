package pl.pw.edu.po.search_engine.simplesearchengine.engine.core;

import java.io.Serializable;
import java.util.*;

public class InvertedIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    // Mapping: term -> (document ID -> term positions)
    private final Map<String, Map<Integer, List<Integer>>> index = new HashMap<>();

    // Forward index: document ID -> content
    private final Map<Integer, String> forwardIndex = new HashMap<>();

    private int nextDocId;

    /**
     * Add documents to the inverted index.
     *
     * @param content processed document content
     * @param tokens tokenized content
     * @return assigned document ID
     */
    public synchronized int addDocument(String content, List<String> tokens) {
        int docId = nextDocId++;
        forwardIndex.put(docId, content);

        for (int position = 0; position < tokens.size(); position++) {
            String term = tokens.get(position);

            index
                    .computeIfAbsent(term, k -> new HashMap<>())
                    .computeIfAbsent(docId, k -> new ArrayList<>())
                    .add(position);
        }

        return docId;
    }

    /**
     * Returns map of documents (id -> positions) containing the term
     */
    public Map<Integer, List<Integer>> getDocumentsForTerm(String term) {
        return index.getOrDefault(term, Collections.emptyMap());
    }

    /**
     * Returns original document content by its ID.
     */
    public String getDocumentById(int docId) {
        return forwardIndex.get(docId);
    }

    /**
     * Returns number of all documents in the index.
     */
    public int getDocumentCount() {
        return forwardIndex.size();
    }

    /**
     * Returns original document content by its ID.
     * (Note: duplicate of getDocumentById - consider removing)
     */
    public String getDocumentCountById(int docId) {
        return forwardIndex.get(docId);
    }

    /**
     * Print whole index
     */
    public void printIndex() {
        index.forEach((term, docs) -> {
            System.out.println(term + " -> " + docs);
        });
    }

    /**
     * Clear all documents from the index (delegation pattern support)
     */
    public synchronized void clear() {
        index.clear();
        forwardIndex.clear();
        nextDocId = 0;
    }

    /**
     * Remove document from index
     * Removes all term entries for the given document ID
     */
    public synchronized void removeDocument(int docId) {
        // 1. Remove from forward index
        forwardIndex.remove(docId);

        // 2. Remove from inverted index (all terms containing this docId)
        index.forEach((term, docMap) -> {
            docMap.remove(docId);
        });

        // 3. Remove empty terms (optional - clean up)
        index.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Merge another index into this one (delegation pattern support)
     * Preserves all documents from the new index with new document IDs
     */
    public synchronized void merge(InvertedIndex other) {
        if (other == null) return;

        // Merge forward index and rebuild inverted index
        other.forwardIndex.forEach((oldDocId, content) -> {
            int newDocId = nextDocId++;
            forwardIndex.put(newDocId, content);
        });

        // Rebuild the inverted index based on forward index
        // Note: We lose term position information during merge, so we reset it
        other.index.forEach((term, docMap) -> {
            index.computeIfAbsent(term, k -> new HashMap<>())
                    .putAll(docMap);
        });
    }
}
