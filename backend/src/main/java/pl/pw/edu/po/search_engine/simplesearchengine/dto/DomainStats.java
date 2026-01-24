package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for domain statistics.
 * Used by Analytics Dashboard to display top domains.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomainStats {
    private String domain;
    private Long count;
}
