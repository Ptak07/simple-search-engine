package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String query;

    @Builder.Default
    private Integer limit = 10;

    @Builder.Default
    private Integer offset = 0;
}

