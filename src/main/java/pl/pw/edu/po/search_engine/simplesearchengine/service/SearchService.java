package pl.pw.edu.po.search_engine.simplesearchengine.service;

import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResult;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.InvertedIndex;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles user search queries and ranking.
 *
 * Responsibilities:
 *  - Preprocess the user's query using the same text analysis pipeline as indexing.
 *  - Retrieve matching documents from the inverted index.
 *  - Compute relevance scores using the TF-IDF algorithm.
 *  - Return sorted search results as DTOs.
 */
@Service
public class SearchService {

    private final TextPreprocessor textPreprocessor;
    private final InvertedIndex invertedIndex;
    private final TfIdfScoringService tfIdfScoringService;

    /**
     * SearchService depends on IndexingService (for access to the shared in memory index)
     * and TfIdfScoringService (for relevance scoring).
     */
    public SearchService(IndexingService indexingService, TfIdfScoringService tfIdfScoringService) {
        this.invertedIndex = indexingService.getInvertedIndex();
        this.textPreprocessor = new TextPreprocessor();
        this.tfIdfScoringService = tfIdfScoringService;
    }

    /**
     * Main entry point for performing a search.
     * It processes the query, retrieves all matching documents,
     * ranks them using TF-IDF, and returns results sorted by relevance.
     *
     * @param query Raw search query from the user
     * @return SearchResponse containing sorted list of results
     */
    public SearchResponse search(String query) {
        // 1️⃣ Preprocess the query (lowercase, stopword removal, stemming)
        List<String> queryTokens = textPreprocessor.process(query);
        if (queryTokens.isEmpty()) return new SearchResponse(List.of());

        // 2️⃣ Collect documents for each term in the query
        List<Set<Integer>> docsPerTerm = queryTokens.stream()
                .map(term -> invertedIndex.getDocumentsForTerm(term).keySet())
                .toList();

        if (docsPerTerm.isEmpty()) return new SearchResponse(List.of());

        // 3️⃣ Find intersection (documents that contain all query terms)
        Set<Integer> matchingDocs = new HashSet<>(docsPerTerm.get(0));
        for (int i = 1; i < docsPerTerm.size(); i++) {
            matchingDocs.retainAll(docsPerTerm.get(i));
        }

        if (matchingDocs.isEmpty()) return new SearchResponse(List.of());

        // 4️⃣ Score each matching document using TF-IDF
        List<SearchResult> results = matchingDocs.stream()
                .map(docId -> {
                    double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);
                    String content = invertedIndex.getDocumentById(docId);
                    return new SearchResult(docId, content, score);
                })
                .sorted(Comparator.comparingDouble(SearchResult::getScore).reversed())
                .collect(Collectors.toList());

        return new SearchResponse(results);
    }
}
