package pl.pw.edu.po.search_engine.simplesearchengine.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.exception.DocumentNotFoundException;
import pl.pw.edu.po.search_engine.simplesearchengine.exception.DuplicateUrlException;
import pl.pw.edu.po.search_engine.simplesearchengine.model.Document;
import pl.pw.edu.po.search_engine.simplesearchengine.repository.DocumentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final IndexingService indexingService;

    /**
     * Add new document (PosgreSQL + InvertedIndex)
     */
    @Transactional
    public Document addDocument(DocumentRequest request) {
        log.info("Adding document: {}", request.getUrl());

        if (documentRepository.existsByUrl(request.getUrl())) {
            throw new DuplicateUrlException("Document with URL already exists: " + request.getUrl());
        }

        Document document = new Document(
                request.getTitle(),
                request.getContent(),
                request.getUrl()
        );
        Document savedDocument = documentRepository.save(document);

        indexingService.addDocument(String.valueOf(savedDocument.getId()), savedDocument.getContent());

        log.info("Document added with ID={}, URL={}", savedDocument.getId(), savedDocument.getUrl());
        return savedDocument;
    }

    /**
     * Download all documents sorted by created date (newest first)
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Download documents by ID
     */
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: ID=" + id));
    }

    /**
     * Download document by URL
     */
    public Document getDocumentByUrl(String url) {
        return documentRepository.findByUrl(url)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: URL=" + url));
    }

    /**
     * Update document
     */
    @Transactional
    public Document updateDocument(Long id, DocumentRequest request) {
        log.info("Updating document ID={}", id);

        Document document = getDocumentById(id);

        if (!document.getUrl().equals(request.getUrl()) &&
            documentRepository.existsByUrl(request.getUrl())) {
            throw new DuplicateUrlException("URL already exists: " + request.getUrl());
        }

        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setUrl(request.getUrl());

        Document updated = documentRepository.save(document);

        indexingService.removeDocument(String.valueOf(id));
        indexingService.addDocument(String.valueOf(id), updated.getContent());

        log.info("Document updated: ID={}", id);
        return updated;
    }

    /**
     * Delete document
     */
    @Transactional
    public void deleteDocument(Long id) {
        log.info("Deleting document: ID={}", id);

        if (!documentRepository.existsById(id)) {
            throw new DocumentNotFoundException("Document not found: ID=" + id);
        }

        documentRepository.deleteById(id);
        indexingService.removeDocument(String.valueOf(id));

        log.info("Document deleted: ID={}", id);
    }

    /**
     * Delete all documents
     */
    @Transactional
    public void deleteAllDocuments() {
        log.info("Deleting all documents");
        documentRepository.deleteAll();
        indexingService.clearIndex();
        log.info("All documents deleted");
    }

    /**
     * Count all documents
     */
    public long countDocuments() {
        return documentRepository.count();
    }

    @Transactional
    public Document addOrUpdateDocument(String url, String title, String content) {
        return documentRepository.findByUrl(url)
                .map(existing -> {
                    existing.setTitle(title);
                    existing.setContent(content);
                    existing.setCrawledAt(LocalDateTime.now());
                    Document updated = documentRepository.save(existing);

                    indexingService.removeDocument(String.valueOf(existing.getId()));
                    indexingService.addDocument(String.valueOf(existing.getId()), content);

                    log.info("Document updated by crawler: ID={}", existing.getId());
                    return updated;
                })
                .orElseGet(() -> {
                    Document newDoc = new Document(title, content, url);
                    newDoc.setCrawledAt(LocalDateTime.now());
                    Document saved = documentRepository.save(newDoc);

                    indexingService.addDocument(String.valueOf(saved.getId()), content);

                    log.info("Document added by crawler: ID={}", saved.getId());
                    return saved;
                });
    }
}
