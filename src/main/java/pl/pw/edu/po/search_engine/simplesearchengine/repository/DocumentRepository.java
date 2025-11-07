package pl.pw.edu.po.search_engine.simplesearchengine.repository;

import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.pw.edu.po.search_engine.simplesearchengine.model.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Find by URL (unique)
    Optional<Document> findByUrl(String url);

    // Check existence by URL
    boolean existsByUrl(String url);

    // Lists of documents sorted by created data (newest first)
    List<Document> findAllByOrderByCreatedAtDesc();

    // Documents added after a specific date
    List<Document> findByCreatedAtAfter(LocalDateTime dateTime);

    // Number of documents with title containing a specific keyword
    @Query("SELECT COUNT(d) FROM Document d WHERE d.title LIKE %?1%")
    long countByTitleContaining(String keyword);
}
