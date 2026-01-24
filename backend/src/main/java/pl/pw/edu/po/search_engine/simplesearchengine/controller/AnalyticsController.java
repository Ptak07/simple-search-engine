package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DataPoint;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DomainStats;
import pl.pw.edu.po.search_engine.simplesearchengine.repository.DocumentRepository;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for analytics and statistics.
 * Provides aggregated data for frontend dashboards and charts.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final DocumentRepository documentRepository;

    /**
     * Get indexed documents count over time within a date range.
     * Used for time-series charts in Analytics Dashboard.
     *
     * @param from start date (inclusive)
     * @param to end date (inclusive)
     * @return list of data points (date, count)
     */
    @GetMapping("/indexed-over-time")
    public ResponseEntity<List<DataPoint>> getIndexedOverTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        log.debug("Fetching indexed documents from {} to {}", from, to);

        Map<LocalDate, Long> counts = documentRepository.findAll().stream()
                .filter(d -> d.getIndexedAt() != null)
                .filter(d -> {
                    LocalDate date = d.getIndexedAt().toLocalDate();
                    return !date.isBefore(from) && !date.isAfter(to);
                })
                .collect(Collectors.groupingBy(
                        d -> d.getIndexedAt().toLocalDate(),
                        Collectors.counting()
                ));

        List<DataPoint> dataPoints = counts.entrySet().stream()
                .map(e -> new DataPoint(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DataPoint::getDate))
                .collect(Collectors.toList());

        log.debug("Found {} data points", dataPoints.size());
        return ResponseEntity.ok(dataPoints);
    }

    /**
     * Get top domains by document count.
     * Used for pie charts or bar charts in Analytics Dashboard.
     *
     * @param limit maximum number of domains to return (default: 10)
     * @return list of domain statistics sorted by count (descending)
     */
    @GetMapping("/top-domains")
    public ResponseEntity<List<DomainStats>> getTopDomains(
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Fetching top {} domains", limit);

        Map<String, Long> domainCounts = documentRepository.findAll().stream()
                .map(d -> {
                    try {
                        String host = URI.create(d.getUrl()).getHost();
                        return host != null ? host : "unknown";
                    } catch (Exception e) {
                        return "unknown";
                    }
                })
                .collect(Collectors.groupingBy(
                        domain -> domain,
                        Collectors.counting()
                ));

        List<DomainStats> topDomains = domainCounts.entrySet().stream()
                .map(e -> new DomainStats(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DomainStats::getCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        log.debug("Found {} unique domains", domainCounts.size());
        return ResponseEntity.ok(topDomains);
    }

    /**
     * Get total statistics summary.
     *
     * @return map with various aggregate statistics
     */
    @GetMapping("/total-stats")
    public ResponseEntity<Map<String, Object>> getTotalStats() {
        log.debug("Fetching total statistics");

        long totalDocs = documentRepository.count();

        long uniqueDomains = documentRepository.findAll().stream()
                .map(d -> {
                    try {
                        return URI.create(d.getUrl()).getHost();
                    } catch (Exception e) {
                        return "unknown";
                    }
                })
                .distinct()
                .count();

        Map<String, Object> stats = Map.of(
                "totalDocuments", totalDocs,
                "uniqueDomains", uniqueDomains
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Get documents indexed in the last N days.
     *
     * @param days number of days to look back (default: 7)
     * @return count of documents indexed in the period
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<Map<String, Object>> getRecentActivity(
            @RequestParam(defaultValue = "7") int days) {

        log.debug("Fetching activity for last {} days", days);

        LocalDate cutoffDate = LocalDate.now().minusDays(days);

        long recentCount = documentRepository.findAll().stream()
                .filter(d -> d.getIndexedAt() != null)
                .filter(d -> !d.getIndexedAt().toLocalDate().isBefore(cutoffDate))
                .count();

        return ResponseEntity.ok(Map.of(
                "days", days,
                "documentsIndexed", recentCount
        ));
    }
}
