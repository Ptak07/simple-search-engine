package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.service.IndexingService;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final IndexingService indexingService;

    // Constructor-based dependency injection
    public SearchController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    /**
     * POST /api/documents
     * Adds a new document to the search index.
     */
    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public String addDocument(@RequestBody DocumentRequest request) {
        int docId = indexingService.index(request);
        return "Document indexed successfully width ID: " + docId;
    }

    /**
     * GET /api/index
     * Lets you view the current state of the inverted index.
     */
    @GetMapping("/index")
    public String printIndex() {
        indexingService.printIndex();
        return "Index printed to console.";
    }

}
