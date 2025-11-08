package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.service.IndexingService;
import pl.pw.edu.po.search_engine.simplesearchengine.service.SearchService;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final IndexingService indexingService;
    private final SearchService searchService;

    // Constructor-based dependency injection
    public SearchController(IndexingService indexingService, SearchService searchService) {
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    // Note: POST /api/documents moved to DocumentController

    /**
     * GET /api/index
     * Lets you view the current state of the inverted index.
     */
    @GetMapping("/index")
    public String printIndex() {
        indexingService.printIndex();
        return "Index printed to console.";
    }

    /**
     * GET /api/search?q=your+query
     * Searches documents and returns ranked results.
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam("q") String query) {
        SearchResponse response = searchService.search(query);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
