package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a single search result in the response.
 *
 * Each result contains:
 *  - documentId: unique identifier (integer)
 *  - content: original text of the document
 *  - score: TF-IDF relevance score
 */
@Data
@AllArgsConstructor
public class SearchResult {
    private int documentId;
    private String content;
    private double score;
}