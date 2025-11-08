package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.model.Document;
import pl.pw.edu.po.search_engine.simplesearchengine.service.DocumentService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> addDocument(@RequestBody DocumentRequest request) {
        log.info("POST /api/documents - Adding document: {}", request.getTitle());
        Document document = documentService.addDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(document));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        log.info("GET /api/documents - Getting all documents");
        List<Document> documents = documentService.getAllDocuments();
        List<DocumentResponse> responses = documents.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id) {
        log.info("GET /api/documents/{} - Getting document by ID", id);
        Document document = documentService.getDocumentById(id);
        return ResponseEntity.ok(toResponse(document));
    }

    @GetMapping("/url")
    public ResponseEntity<DocumentResponse> getDocumentByUrl(@RequestParam String url) {
        log.info("GET /api/documents/url?url={} - Getting document by URL", url);
        Document document = documentService.getDocumentByUrl(url);
        return ResponseEntity.ok(toResponse(document));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentRequest request) {
        log.info("PUT /api/documents/{} - Updating document", id);
        Document document = documentService.updateDocument(id, request);
        return ResponseEntity.ok(toResponse(document));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        log.info("DELETE /api/documents/{} - Deleting document", id);
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countDocuments() {
        log.info("GET /api/documents/count - Counting documents");
        long count = documentService.countDocuments();
        return ResponseEntity.ok(count);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllDocuments() {
        log.info("DELETE /api/documents - Deleting all documents");
        documentService.deleteAllDocuments();
        return ResponseEntity.noContent().build();
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .url(document.getUrl())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}

