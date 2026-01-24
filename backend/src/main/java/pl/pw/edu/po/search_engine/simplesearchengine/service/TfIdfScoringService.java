package pl.pw.edu.po.search_engine.simplesearchengine.service;

import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.SearchIndex;

import java.util.*;

/**
 * Service responsible for calculating document relevance scores using the TF-IDF algorithm.
 * Uses constructor injection for all dependencies (proper Spring DI).
 * Decoupled from IndexingService - directly uses SearchIndex.
 */
@Service
public class TfIdfScoringService {

    private final SearchIndex searchIndex;
    private final TextPreprocessor textPreprocessor;

    /**
     * Constructor injection - all dependencies provided by Spring.
     * @param searchIndex Thread-safe search index bean
     * @param textPreprocessor English text preprocessor bean
     */
    public TfIdfScoringService(SearchIndex searchIndex, TextPreprocessor textPreprocessor) {
        this.searchIndex = searchIndex;
        this.textPreprocessor = textPreprocessor;
    }

    /**
     * Calculates document relevance using TF-IDF with consistent text processing.
     * - TF uses the number of occurrences divided by the document length
     *   (document length counted AFTER the same preprocessing pipeline).
     * - IDF uses a smoothed logarithm to avoid extreme values.
     */
    public double calculateTfIdfScore(int docId, List<String> queryTokens) {

        // Defensive guards
        int totalDocs = Math.max(1, searchIndex.getDocumentCount());
        String content = searchIndex.getDocumentById(docId);
        if (content == null || content.isBlank()) return 0.0;

        // Count document length AFTER the same preprocessing as used at indexing time.
        // This ensures TF denominator matches how postings were created.
        int docLen = textPreprocessor.process(content).size();
        if (docLen == 0) return 0.0;

        // Using unique query terms avoids overcounting repeated words in the user query.
        // (You can switch to raw list if you want repetition to matter.)
        Set<String> uniqueQueryTerms = new LinkedHashSet<>(queryTokens);

        double score = 0.0;
        for (String term : uniqueQueryTerms) {
            Map<Integer, List<Integer>> postings = searchIndex.getDocumentsForTerm(term);
            List<Integer> positions = postings.get(docId);
            if (positions == null || positions.isEmpty()) continue;

            // ----- TF (term frequency) -----
            // Raw count of this term in the document:
            int tfRaw = positions.size();

            // Normalized TF: occurrences divided by preprocessed document length.
            double tf = (double) tfRaw / (double) docLen;

            // ----- IDF (inverse document frequency) -----
            // Document frequency: in how many documents the term appears
            int df = Math.max(1, postings.size());

            // Smoothed IDF to avoid division-by-zero and dampen extremes:
            // idf = ln(1 + N / df)
            double idf = Math.log(1.0 + ((double) totalDocs / (double) df));

            // ----- TF-IDF -----
            score += tf * idf;
        }

        return score;
    }
}
