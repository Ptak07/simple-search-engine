package pl.pw.edu.po.search_engine.simplesearchengine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TfIdfScoringService.
 * Tests TF-IDF score calculation.
 */
class TfIdfScoringServiceTest {

    private IndexingService indexingService;
    private TfIdfScoringService tfIdfScoringService;

    @BeforeEach
    void setUp() {
        indexingService = new IndexingService();
        tfIdfScoringService = new TfIdfScoringService(indexingService);
    }

    @Test
    void testCalculateScoreForSingleDocument() {
        int docId = indexingService.index(new DocumentRequest("1", "machine learning algorithms"));

        List<String> queryTokens = List.of("machin", "learn"); // Stemmed forms
        double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        assertTrue(score > 0, "Score should be positive for matching terms");
    }

    @Test
    void testCalculateScoreForNonExistentDocument() {
        indexingService.index(new DocumentRequest("1", "test document"));

        List<String> queryTokens = List.of("test");
        double score = tfIdfScoringService.calculateTfIdfScore(999, queryTokens);

        assertEquals(0.0, score, "Score for non-existent document should be 0");
    }

    @Test
    void testCalculateScoreWithNoMatchingTerms() {
        int docId = indexingService.index(new DocumentRequest("1", "machine learning"));

        List<String> queryTokens = List.of("quantum", "computing");
        double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        assertEquals(0.0, score, "Score should be 0 when no terms match");
    }

    @Test
    void testCalculateScoreWithEmptyQuery() {
        int docId = indexingService.index(new DocumentRequest("1", "test document"));

        List<String> queryTokens = List.of();
        double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        assertEquals(0.0, score, "Score should be 0 for empty query");
    }

    @Test
    void testCalculateScoreWithPartialMatch() {
        int docId = indexingService.index(new DocumentRequest("1", "machine learning algorithms"));

        // Only "machine" is in the document (after stemming)
        List<String> queryTokens = List.of("machin", "quantum");
        double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        assertTrue(score > 0, "Score should be positive even with partial match");
    }

    @Test
    void testScoreIncreasesWithTermFrequency() {
        // Document with term appearing once
        int docId1 = indexingService.index(new DocumentRequest("1", "java programming"));

        // Document with term appearing multiple times
        int docId2 = indexingService.index(new DocumentRequest("2", "java java java programming"));

        List<String> queryTokens = List.of("java");

        double score1 = tfIdfScoringService.calculateTfIdfScore(docId1, queryTokens);
        double score2 = tfIdfScoringService.calculateTfIdfScore(docId2, queryTokens);

        assertTrue(score2 > score1, "Document with higher term frequency should have higher score");
    }

    @Test
    void testScoreDecreasesWithDocumentFrequency() {
        // Add documents - first with unique term, others with common term
        int docId1 = indexingService.index(new DocumentRequest("1", "unique programming"));
        indexingService.index(new DocumentRequest("2", "common programming"));
        indexingService.index(new DocumentRequest("3", "common test"));
        indexingService.index(new DocumentRequest("4", "common example"));

        // Score for rare term
        double scoreUnique = tfIdfScoringService.calculateTfIdfScore(docId1, List.of("uniqu")); // stemmed

        // Score for common term
        double scoreCommon = tfIdfScoringService.calculateTfIdfScore(docId1, List.of("common"));

        // Unique term should have higher IDF, thus higher score
        assertTrue(scoreUnique > scoreCommon, "Rare terms should have higher IDF score");
    }

    @Test
    void testScoreNormalizedByDocumentLength() {
        // Short document
        int docId1 = indexingService.index(new DocumentRequest("1", "java"));

        // Long document with same term frequency but more words
        int docId2 = indexingService.index(new DocumentRequest("2",
                "java programming language used for enterprise application development"));

        List<String> queryTokens = List.of("java");

        double score1 = tfIdfScoringService.calculateTfIdfScore(docId1, queryTokens);
        double score2 = tfIdfScoringService.calculateTfIdfScore(docId2, queryTokens);

        assertTrue(score1 > score2, "Shorter documents should have higher TF score (normalized)");
    }

    @Test
    void testScoreWithMultipleQueryTerms() {
        int docId = indexingService.index(new DocumentRequest("1", "machine learning algorithms"));

        // Single term query
        double scoreSingle = tfIdfScoringService.calculateTfIdfScore(docId, List.of("machin"));

        // Multiple term query
        double scoreMultiple = tfIdfScoringService.calculateTfIdfScore(docId, List.of("machin", "learn"));

        assertTrue(scoreMultiple > scoreSingle, "More matching terms should increase score");
    }

    @Test
    void testScoreWithDuplicateQueryTerms() {
        int docId = indexingService.index(new DocumentRequest("1", "machine learning"));

        // Query with duplicate terms (using unique set internally)
        List<String> queryTokens = List.of("machin", "machin", "machin");
        double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        // Should not count duplicates multiple times
        assertTrue(score > 0);
    }

    @Test
    void testScoreConsistency() {
        int docId = indexingService.index(new DocumentRequest("1", "test document"));

        List<String> queryTokens = List.of("test");

        double score1 = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);
        double score2 = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        assertEquals(score1, score2, 0.0001, "Multiple calls should return same score");
    }

    @Test
    void testScoreForEmptyDocument() {
        int docId = indexingService.index(new DocumentRequest("1", ""));

        List<String> queryTokens = List.of("test");
        double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        assertEquals(0.0, score, "Score for empty document should be 0");
    }

    @Test
    void testScoreForDocumentWithStopWordsOnly() {
        int docId = indexingService.index(new DocumentRequest("1", "the and or but"));

        // These are stop words and will be removed during indexing
        List<String> queryTokens = List.of("test");
        double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        assertEquals(0.0, score, "Document with only stop words has no real content");
    }

    @Test
    void testTfIdfWithMultipleDocuments() {
        int docId1 = indexingService.index(new DocumentRequest("1", "machine learning"));
        int docId2 = indexingService.index(new DocumentRequest("2", "deep learning"));
        int docId3 = indexingService.index(new DocumentRequest("3", "machine intelligence"));

        List<String> queryTokens = List.of("machin", "learn");

        // Only doc1 has both terms
        double score1 = tfIdfScoringService.calculateTfIdfScore(docId1, queryTokens);
        double score2 = tfIdfScoringService.calculateTfIdfScore(docId2, queryTokens);
        double score3 = tfIdfScoringService.calculateTfIdfScore(docId3, queryTokens);

        assertTrue(score1 > 0, "Doc 1 should have positive score (has both terms)");
        assertTrue(score2 > 0, "Doc 2 should have positive score (has learning)");
        assertTrue(score3 > 0, "Doc 3 should have positive score (has machine)");
        assertTrue(score1 > score2, "Doc 1 should score higher than doc 2");
        assertTrue(score1 > score3, "Doc 1 should score higher than doc 3");
    }

    @Test
    void testScorePositiveForAnyMatch() {
        int docId = indexingService.index(new DocumentRequest("1", "artificial intelligence"));

        List<String> queryTokens = List.of("artifici"); // stemmed form
        double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        assertTrue(score > 0, "Any matching term should produce positive score");
    }

    @Test
    void testScoreWithLongDocument() {
        StringBuilder longDoc = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longDoc.append("word").append(i).append(" ");
        }
        longDoc.append("target");

        int docId = indexingService.index(new DocumentRequest("1", longDoc.toString()));

        List<String> queryTokens = List.of("target");
        double score = tfIdfScoringService.calculateTfIdfScore(docId, queryTokens);

        assertTrue(score > 0, "Should find term in long document");
    }

    @Test
    void testIdfIncreaseWithMoreDocuments() {
        // Add first document
        int docId1 = indexingService.index(new DocumentRequest("1", "unique term"));
        double score1 = tfIdfScoringService.calculateTfIdfScore(docId1, List.of("uniqu"));

        // Add more documents without the term
        indexingService.index(new DocumentRequest("2", "other content"));
        indexingService.index(new DocumentRequest("3", "more content"));
        indexingService.index(new DocumentRequest("4", "different content"));

        // IDF should increase because N (total docs) increased but df stayed the same
        double score2 = tfIdfScoringService.calculateTfIdfScore(docId1, List.of("uniqu"));

        assertTrue(score2 > score1, "IDF should increase as total document count increases");
    }
}

