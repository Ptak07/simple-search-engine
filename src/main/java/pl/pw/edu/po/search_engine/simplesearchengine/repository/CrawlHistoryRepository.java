package pl.pw.edu.po.search_engine.simplesearchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pw.edu.po.search_engine.simplesearchengine.model.CrawlHistory;

public interface CrawlHistoryRepository extends JpaRepository<CrawlHistory, Long> {
}
