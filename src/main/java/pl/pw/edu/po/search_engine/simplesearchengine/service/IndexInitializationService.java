package pl.pw.edu.po.search_engine.simplesearchengine.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.model.Document;
import pl.pw.edu.po.search_engine.simplesearchengine.repository.DocumentRepository;

import java.util.List;

/**
 * Initializes InvertedIndex from PostgreSQL on application startup.
 * Replaces the old PersistenceService (index.ser approach).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IndexInitializationService {

    private final DocumentRepository documentRepository;
    private final IndexingService indexingService;

    /**
     * Rebuild InvertedIndex from PostgreSQL on startup
     */
    @PostConstruct
    public void initializeIndex() {
        log.info("Initializing InvertedIndex from database...");

        List<Document> allDocuments = documentRepository.findAll();

        if (allDocuments.isEmpty()) {
            log.info("No documents found in database. Starting with empty index.");
            return;
        }

        // Rebuild index from all documents
        allDocuments.forEach(doc -> {
            indexingService.addDocument(String.valueOf(doc.getId()), doc.getContent());
        });

        log.info("InvertedIndex initialized with {} documents", allDocuments.size());
    }
}

