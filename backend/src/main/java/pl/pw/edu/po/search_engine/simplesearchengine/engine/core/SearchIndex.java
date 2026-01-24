package pl.pw.edu.po.search_engine.simplesearchengine.engine.core;

import java.util.List;
import java.util.Map;

/**
 * Contract for search index operations.
 * Defines thread-safe operations for document indexing and retrieval.
 * Implementations must ensure thread-safety for all operations.
 */
public interface SearchIndex {

    /**
     * Add document with auto-generated ID.
     *
     * @param content processed document content
     * @param tokens tokenized content
     * @return assigned document ID
     */
    int addDocument(String content, List<String> tokens);

    /**
     * Add document with specific ID (for database integration).
     *
     * @param docId specific document ID
     * @param content document content
     * @param tokens tokenized content
     */
    void addDocument(int docId, String content, List<String> tokens);

    /**
     * Returns immutable map of documents (id -> positions) containing the term.
     *
     * @param term search term
     * @return unmodifiable map of document IDs to term positions
     */
    Map<Integer, List<Integer>> getDocumentsForTerm(String term);

    /**
     * Returns original document content by its ID.
     *
     * @param docId document ID
     * @return document content or null if not found
     */
    String getDocumentById(int docId);

    /**
     * Returns number of all documents in the index.
     *
     * @return document count
     */
    int getDocumentCount();

    /**
     * Remove document from index.
     * Must efficiently remove all term entries for the given document.
     *
     * @param docId document ID to remove
     */
    void removeDocument(int docId);

    /**
     * Clear all documents from the index.
     */
    void clear();

    /**
     * Merge another index into this one.
     * Preserves all documents from the other index with new document IDs.
     *
     * @param other index to merge
     */
    void merge(SearchIndex other);
}

