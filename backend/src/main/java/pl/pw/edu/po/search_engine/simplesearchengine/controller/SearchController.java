package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.service.SearchService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    /**
     * GET /api/search?query=...&limit=10&offset=0&language=pl
     * Searches documents with pagination and returns ranked results with snippets.
     *
     * @param query search query
     * @param limit number of results per page
     * @param offset starting position
     * @param language language code ("pl" or "en"), defaults to "pl"
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = true) String query,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "pl") String language) {

        log.info("GET /api/search - query: '{}', limit: {}, offset: {}, language: {}",
                query, limit, offset, language);

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .limit(limit)
                .offset(offset)
                .language(language)
                .build();

        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

}
