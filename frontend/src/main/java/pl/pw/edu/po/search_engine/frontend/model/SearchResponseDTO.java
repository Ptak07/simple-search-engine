package pl.pw.edu.po.search_engine.frontend.model;

import java.util.List;

/**
 * Frontend DTO for search response (mirrors backend SearchResponse).
 */
public class SearchResponseDTO {
    private String query;
    private Long totalResults;
    private Integer limit;
    private Integer offset;
    private List<SearchResultDTO> results;
    private Long searchTimeMs;

    public SearchResponseDTO() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Long totalResults) {
        this.totalResults = totalResults;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public List<SearchResultDTO> getResults() {
        return results;
    }

    public void setResults(List<SearchResultDTO> results) {
        this.results = results;
    }

    public Long getSearchTimeMs() {
        return searchTimeMs;
    }

    public void setSearchTimeMs(Long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
    }
}
