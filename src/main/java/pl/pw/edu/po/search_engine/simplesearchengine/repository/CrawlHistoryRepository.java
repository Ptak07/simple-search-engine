package pl.pw.edu.po.search_engine.simplesearchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pw.edu.po.search_engine.simplesearchengine.model.CrawlHistory;

import java.util.List;

/**
 * Repository for CrawlHistory entity.
 * Spring Data JPA automatically implements query methods based on method names.
 */
public interface CrawlHistoryRepository extends JpaRepository<CrawlHistory, Long> {

    /**
     * Find crawl history records by status.
     * Spring Data JPA automatically generates SQL: SELECT * FROM crawl_history WHERE status = ?
     *
     * @param status the status to filter by (e.g., "SUCCESS", "FAILED", "PARTIAL", "STARTED")
     * @return list of crawl history records with the given status
     */
    List<CrawlHistory> findByStatus(String status);
}
