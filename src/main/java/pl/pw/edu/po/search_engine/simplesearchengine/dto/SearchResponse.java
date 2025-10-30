package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {
    private List<SearchResult> results;
}
