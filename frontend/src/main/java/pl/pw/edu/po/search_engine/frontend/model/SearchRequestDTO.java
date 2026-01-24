package pl.pw.edu.po.search_engine.frontend.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Frontend DTO for search requests.
 */
public class SearchRequestDTO {
    private String query;
    private Integer limit = 10;
    private Integer offset = 0;
    private String language = "pl";
    private List<String> domains;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String contentType;

    public SearchRequestDTO() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SearchRequestDTO dto = new SearchRequestDTO();

        public Builder query(String query) {
            dto.query = query;
            return this;
        }

        public Builder limit(Integer limit) {
            dto.limit = limit;
            return this;
        }

        public Builder offset(Integer offset) {
            dto.offset = offset;
            return this;
        }

        public Builder language(String language) {
            dto.language = language;
            return this;
        }

        public SearchRequestDTO build() {
            return dto;
        }
    }
}
