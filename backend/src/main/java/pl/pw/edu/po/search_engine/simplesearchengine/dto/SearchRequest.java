package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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

    @Builder.Default
    private String language = "pl";

    // NOWE POLA dla faceted search
    private List<String> domains;      // Filter by domain (e.g., ["wikipedia.org", "github.com"])
    private LocalDate fromDate;        // Filter by indexed date (from)
    private LocalDate toDate;          // Filter by indexed date (to)
    private String contentType;        // Filter by content type (e.g., "HTML", "PDF")
}



