package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final SearchService searchService;

    // Constructor-based dependency injection
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * POST /api/documents
     * Adds a new document to the search index.
     */
    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public void addDocument(DocumentRequest documentRequest) {
        searchService.indexDocument(documentRequest);
    }

    /**
     * GET /api/search?q=term
     * Executes a search query and returns ranked results.
     */
    @GetMapping("/search")
    public SearchResponse search(@RequestParam("q") String query) {
        return searchService.search(query);
    }

    /**
     * Simple heatlth check endpoint.
     */
    @GetMapping("/ping")
    public String ping() {
        return "Search API is running.";
    }
}
