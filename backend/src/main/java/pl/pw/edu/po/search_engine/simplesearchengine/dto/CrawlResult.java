package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResult {

    private String status;              // ← "SUCCESS", "FAILED", "PARTIAL"
    private Integer pagesProcessed;
    private Integer documentsIndexed;
    private Integer errorCount;
    private List<String> errors;
    private Long crawlTimeMs;
}
