package pl.pw.edu.po.search_engine.simplesearchengine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String startUrl;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(nullable = false)
    private String status; // "STARTED", "SUCCESS", "PARTIAL", "FAILED"

    @Column(name = "pages_crawled", nullable = false)
    private int pagesCrawled;

    @Column(name = "documents_indexed", nullable = false)
    private int documentsIndexed;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(length = 2048)
    private String errorMessage;

    @PrePersist
    private void prePersist() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "STARTED";
        }
    }
}
