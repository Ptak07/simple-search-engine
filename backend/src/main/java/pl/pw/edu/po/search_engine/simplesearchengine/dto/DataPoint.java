package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for time-series data points.
 * Used by Analytics Dashboard for charts (e.g., indexed pages over time).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataPoint {
    private LocalDate date;
    private Long count;
}
