package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResult {
    private String documentId;
    private double score;
}
