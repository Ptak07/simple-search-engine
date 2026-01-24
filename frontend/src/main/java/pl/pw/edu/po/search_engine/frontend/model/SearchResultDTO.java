package pl.pw.edu.po.search_engine.frontend.model;

import java.util.List;

/**
 * Frontend DTO for search results (mirrors backend SearchResult).
 */
public class SearchResultDTO {
    private DocumentDTO document;
    private Double score;
    private List<String> matchedTerms;
    private String snippet;

    public SearchResultDTO() {
    }

    public DocumentDTO getDocument() {
        return document;
    }

    public void setDocument(DocumentDTO document) {
        this.document = document;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public List<String> getMatchedTerms() {
        return matchedTerms;
    }

    public void setMatchedTerms(List<String> matchedTerms) {
        this.matchedTerms = matchedTerms;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
