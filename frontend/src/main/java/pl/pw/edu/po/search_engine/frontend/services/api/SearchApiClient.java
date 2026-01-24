package pl.pw.edu.po.search_engine.frontend.services.api;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.pw.edu.po.search_engine.frontend.model.SearchRequestDTO;
import pl.pw.edu.po.search_engine.frontend.model.SearchResponseDTO;

/**
 * REST client for Search API endpoints.
 * Uses WebClient for non-blocking HTTP calls.
 */
@Service
public class SearchApiClient {

    private final WebClient webClient;

    public SearchApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Perform search with filters.
     *
     * @param request search request with query and filters
     * @return search response with results
     */
    public SearchResponseDTO search(SearchRequestDTO request) {
        System.out.println("Executing search: query=" + request.getQuery() + ", limit=" + request.getLimit());

        try {
            SearchResponseDTO response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/search")
                            .queryParam("query", request.getQuery())
                            .queryParam("limit", request.getLimit() != null ? request.getLimit() : 10)
                            .queryParam("offset", request.getOffset() != null ? request.getOffset() : 0)
                            .queryParam("language", request.getLanguage() != null ? request.getLanguage() : "pl")
                            .build())
                    .retrieve()
                    .bodyToMono(SearchResponseDTO.class)
                    .doOnError(error -> {
                        System.err.println("WebClient error details: " + error.getClass().getName());
                        System.err.println("Error message: " + error.getMessage());
                        if (error.getCause() != null) {
                            System.err.println("Cause: " + error.getCause().getMessage());
                        }
                        error.printStackTrace();
                    })
                    .block();

            System.out.println("Search successful! Results: " + (response != null && response.getResults() != null ? response.getResults().size() : 0));
            return response;

        } catch (Exception e) {
            System.err.println("Search API call failed: " + e.getMessage());
            System.err.println("Exception type: " + e.getClass().getName());
            e.printStackTrace();
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Perform simple search (query only).
     *
     * @param query search query
     * @param language language code (pl/en)
     * @return search response
     */
    public SearchResponseDTO simpleSearch(String query, String language) {
        SearchRequestDTO request = SearchRequestDTO.builder()
                .query(query)
                .language(language)
                .limit(50)
                .offset(0)
                .build();

        return search(request);
    }
}
