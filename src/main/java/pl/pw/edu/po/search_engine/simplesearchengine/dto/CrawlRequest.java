package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlRequest {

    private String startUrl;

    @Builder.Default
    private Integer maxPages = 10;

    @Builder.Default
    private Integer maxDepth = 2;

    @Builder.Default
    private Boolean respectRobotsTxt = true;

    @Builder.Default
    private Long delayMs = 1000L; // Wait 1s between requests
}
