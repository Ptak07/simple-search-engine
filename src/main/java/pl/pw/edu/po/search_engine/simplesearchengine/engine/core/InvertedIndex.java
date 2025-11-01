package pl.pw.edu.po.search_engine.simplesearchengine.engine.core;

import java.util.*;

public class InvertedIndex {

    // Mapping: term -> (document ID -> term frequency)
    private final Map<String, Map<Integer, List<Integer>>> index = new HashMap<>();

    // Forward index: document ID -> content
    private final Map<Integer, String> forwardIndex = new HashMap<>();

    private int nextDocId;

    /*
     * Add documents to the inverted index.
     *
     * @param content processed document content
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

    /*
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
     * Print whole index
     */
    public void printIndex() {
        index.forEach((term, docs) -> {
            System.out.println(term + " -> " + docs);
        });
    }
}
