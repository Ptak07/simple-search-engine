package pl.pw.edu.po.search_engine.simplesearchengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.*;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.InvertedIndex;
import pl.pw.edu.po.search_engine.simplesearchengine.model.Document;
import pl.pw.edu.po.search_engine.simplesearchengine.repository.DocumentRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final IndexingService indexingService;
    private final TfIdfScoringService tfIdfScoringService;
    private final DocumentRepository documentRepository;
    private final TextPreprocessor textPreprocessor = new TextPreprocessor();

    /**
     * Mai search endpoint with pagination, snippets, and full document data.
     */
    public SearchResponse search(SearchRequest request) {
        long startTime = System.currentTimeMillis();

        log.info("Searching for: {} (limit: {}, offset: {})",
                request.getQuery(), request.getLimit(), request.getOffset());

        // 1. Tokenize query
        List<String> queryTokens = textPreprocessor.process(request.getQuery());
        if (queryTokens.isEmpty()) {
            return buildEmptyResponse(request, startTime);
        }

        // 2. Find matching document IDs from inverted index
        Set<Integer> matchingDocIds = findMatchingDocuments(queryTokens);
        if(matchingDocIds.isEmpty()) {
            return buildEmptyResponse(request, startTime);
        }

        // 3. Fetch fill documetns from PostgreSQL
        List<Long> docsIdsLong = matchingDocIds.stream()
                .map(Integer::longValue)
                .toList();

        List<Document> documents = documentRepository.findAllById(docsIdsLong);

        // 4. Score documents and create result
        List<SearchResult> allResults = documents.stream()
                .map(doc -> createSearchResult(doc, queryTokens))
                .filter(result -> result.getScore() > 0)
                .sorted(Comparator.comparingDouble(SearchResult::getScore).reversed())
                .toList();

        // 5. Apply pagination
        List<SearchResult> paginatedResult = allResults.stream()
                .skip(request.getOffset())
                .limit(request.getLimit())
                .toList();

        long searchTimeMs = System.currentTimeMillis() - startTime;
        log.info("Search completed in {} ms. Found {} results", searchTimeMs, allResults.size());

        return SearchResponse.builder()
                .query(request.getQuery())
                .totalResults((long) allResults.size())
                .limit(request.getLimit())
                .offset(request.getOffset())
                .results(paginatedResult)
                .searchTimeMs(searchTimeMs)
                .build();
    }

    /**
     * Find documents containing ALL query tokens.
     */
    private Set<Integer> findMatchingDocuments(List<String> queryTokens) {
        InvertedIndex invertedIndex = indexingService.getInvertedIndex();

        List<Set<Integer>> docsPerTerm = queryTokens.stream()
                .map(term -> invertedIndex.getDocumentsForTerm(term).keySet())
                .toList();

        if (docsPerTerm.isEmpty()) return Set.of();

        Set<Integer> matchingDocs = new HashSet<>(docsPerTerm.getFirst());
        for (int i = 1; i < docsPerTerm.size(); i++) {
            matchingDocs.retainAll(docsPerTerm.get(i));
        }

        return matchingDocs;
    }

    private SearchResult createSearchResult(Document document, List<String> queryTokens) {
        // Calculate TF-IDF score
        double score = tfIdfScoringService.calculateTfIdfScore(document.getId().intValue(), queryTokens);

        // Find which terms matched
        List<String> docTokens = textPreprocessor.process(
                document.getTitle() + " " + document.getContent()
        );
        List<String> matchedTerms = queryTokens.stream()
                .filter(docTokens::contains)
                .distinct()
                .toList();

        // Create snippet with context
        String snippet = createSnippet(document.getContent(), matchedTerms, 200);

        // Bonus scorefor title matches
        String titleLower = document.getTitle().toLowerCase();
        for (String term : matchedTerms) {
            if (titleLower.contains(term)) {
                score *= 1.3;
            }
        }

        return SearchResult.builder()
                .document(toDocumentResponse(document))
                .score(Math.round(score * 100.0) / 100.0)
                .matchedTerms(matchedTerms)
                .snippet(snippet)
                .build();
    }

    /**
     * Create text snippet wit context around matched terms.
     */
    private String createSnippet(String content, List<String> matchedTerms, int maxLength) {
        if (content == null || content.isBlank()) {
            return "";
        }

        if (matchedTerms.isEmpty()) {
            return content.substring(0, Math.min(maxLength, content.length())) + "...";
        }

        String lowerContent = content.toLowerCase();
        int firstMatch = Integer.MAX_VALUE;

        // Find first occurence of any matched term
        for (String term : matchedTerms) {
            int index = lowerContent.indexOf(term.toLowerCase());
            if (index != -1 && index < firstMatch) {
                firstMatch = index;
            }
        }

        if (firstMatch == Integer.MAX_VALUE) {
            return content.substring(0, Math.min(maxLength, content.length())) + "...";
        }

        // Create snippet with 50 chars before match, rest after.
        int start = Math.max(0, firstMatch - 50);
        int end = Math.min(content.length(), start + maxLength);

        String snippet = content.substring(start, end);

        if (start > 0) snippet = "..." + snippet;
        if (end < content.length()) snippet = snippet + "...";

        return snippet;
    }

    /**
     * Build empty response when no results found.
     */
    private SearchResponse buildEmptyResponse(SearchRequest request, long startTime) {
        return SearchResponse.builder()
                .query(request.getQuery())
                .totalResults(0L)
                .limit(request.getLimit())
                .offset(request.getOffset())
                .results(Collections.emptyList())
                .searchTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }

    /**
     * Convert Document entity to DocumentResponse DTO.
     */
    private DocumentResponse toDocumentResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .url(document.getUrl())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
