package pl.pw.edu.po.search_engine.frontend.model;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * DTO for crawl history data from backend API.
 */
public class CrawlHistoryDTO {

    private Long id;
    private String startUrl;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String status;           // "STARTED", "SUCCESS", "PARTIAL", "FAILED"
    private int pagesCrawled;
    private int documentsIndexed;
    private Long durationMs;
    private String errorMessage;

    public CrawlHistoryDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPagesCrawled() {
        return pagesCrawled;
    }

    public void setPagesCrawled(int pagesCrawled) {
        this.pagesCrawled = pagesCrawled;
    }

    public int getDocumentsIndexed() {
        return documentsIndexed;
    }

    public void setDocumentsIndexed(int documentsIndexed) {
        this.documentsIndexed = documentsIndexed;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Check if crawl is still active.
     */
    public boolean isActive() {
        return "STARTED".equals(status);
    }

    /**
     * Calculate progress percentage.
     */
    public double getProgress(int maxPages) {
        if (maxPages <= 0) return 0;
        return Math.min(100.0, (double) pagesCrawled / maxPages * 100);
    }

    /**
     * Get formatted duration string (HH:MM:SS).
     */
    public String getFormattedDuration() {
        if (durationMs == null) {
            // Calculate from start time if still running
            if (startedAt != null && isActive()) {
                long ms = Duration.between(startedAt, LocalDateTime.now()).toMillis();
                return formatDuration(ms);
            }
            return "--:--:--";
        }
        return formatDuration(durationMs);
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Get human-readable status with emoji.
     */
    public String getStatusWithEmoji() {
        return switch (status) {
            case "STARTED" -> "In Progress";
            case "SUCCESS" -> "✅ Success";
            case "FAILED" -> "❌ Failed";
            case "PARTIAL" -> "⚠️ Partial";
            default -> "❓ Unknown";
        };
    }
}
