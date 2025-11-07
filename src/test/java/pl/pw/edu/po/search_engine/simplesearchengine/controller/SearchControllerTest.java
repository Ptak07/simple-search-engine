package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResult;
import pl.pw.edu.po.search_engine.simplesearchengine.service.IndexingService;
import pl.pw.edu.po.search_engine.simplesearchengine.service.SearchService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SearchController.
 * Tests REST API endpoints.
 */
@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IndexingService indexingService;

    @MockitoBean
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(indexingService, searchService);
    }

    @Test
    void testAddDocument() throws Exception {
        DocumentRequest request = new DocumentRequest("1", "Test document content");
        when(indexingService.index(any(DocumentRequest.class))).thenReturn(0);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("indexed successfully")));

        verify(indexingService, times(1)).index(any(DocumentRequest.class));
    }

    @Test
    void testAddDocumentWithEmptyContent() throws Exception {
        DocumentRequest request = new DocumentRequest("1", "");
        when(indexingService.index(any(DocumentRequest.class))).thenReturn(0);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(indexingService, times(1)).index(any(DocumentRequest.class));
    }

    @Test
    void testAddMultipleDocuments() throws Exception {
        DocumentRequest request1 = new DocumentRequest("1", "First document");
        DocumentRequest request2 = new DocumentRequest("2", "Second document");

        when(indexingService.index(any(DocumentRequest.class))).thenReturn(0, 1);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        verify(indexingService, times(2)).index(any(DocumentRequest.class));
    }

    @Test
    void testPrintIndex() throws Exception {
        doNothing().when(indexingService).printIndex();

        mockMvc.perform(get("/api/index"))
                .andExpect(status().isOk())
                .andExpect(content().string("Index printed to console."));

        verify(indexingService, times(1)).printIndex();
    }

    @Test
    void testSearchWithResults() throws Exception {
        SearchResult result1 = new SearchResult(0, "Machine learning document", 0.95);
        SearchResult result2 = new SearchResult(1, "Deep learning document", 0.85);
        SearchResponse response = new SearchResponse(List.of(result1, result2));

        when(searchService.search(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", "machine learning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(2))
                .andExpect(jsonPath("$.results[0].documentId").value(0))
                .andExpect(jsonPath("$.results[0].score").value(0.95))
                .andExpect(jsonPath("$.results[1].documentId").value(1))
                .andExpect(jsonPath("$.results[1].score").value(0.85));

        verify(searchService, times(1)).search("machine learning");
    }

    @Test
    void testSearchWithNoResults() throws Exception {
        SearchResponse response = new SearchResponse(List.of());

        when(searchService.search(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", "nonexistent term"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(0));

        verify(searchService, times(1)).search("nonexistent term");
    }

    @Test
    void testSearchWithEmptyQuery() throws Exception {
        SearchResponse response = new SearchResponse(List.of());

        when(searchService.search(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(0));

        verify(searchService, times(1)).search("");
    }

    @Test
    void testSearchWithSpecialCharacters() throws Exception {
        SearchResponse response = new SearchResponse(List.of());

        when(searchService.search(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", "test@123!"))
                .andExpect(status().isOk());

        verify(searchService, times(1)).search("test@123!");
    }

    @Test
    void testSearchWithMultipleWords() throws Exception {
        SearchResult result = new SearchResult(0, "Content about machine learning", 0.9);
        SearchResponse response = new SearchResponse(List.of(result));

        when(searchService.search(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", "machine learning algorithms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(1));

        verify(searchService, times(1)).search("machine learning algorithms");
    }

    @Test
    void testSearchWithSpaces() throws Exception {
        SearchResponse response = new SearchResponse(List.of());

        when(searchService.search(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", "  test  query  "))
                .andExpect(status().isOk());

        verify(searchService, times(1)).search("  test  query  ");
    }


    @Test
    void testSearchResultsOrdered() throws Exception {
        SearchResult result1 = new SearchResult(0, "Most relevant", 0.99);
        SearchResult result2 = new SearchResult(1, "Less relevant", 0.75);
        SearchResult result3 = new SearchResult(2, "Least relevant", 0.50);
        SearchResponse response = new SearchResponse(List.of(result1, result2, result3));

        when(searchService.search(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].score").value(0.99))
                .andExpect(jsonPath("$.results[1].score").value(0.75))
                .andExpect(jsonPath("$.results[2].score").value(0.50));
    }

    @Test
    void testAddDocumentReturnsDocumentId() throws Exception {
        DocumentRequest request = new DocumentRequest("test-id", "Test content");
        when(indexingService.index(any(DocumentRequest.class))).thenReturn(42);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("42")));
    }

    @Test
    void testSearchContentType() throws Exception {
        SearchResponse response = new SearchResponse(List.of());
        when(searchService.search(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testAddDocumentWithLongContent() throws Exception {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("word").append(i).append(" ");
        }

        DocumentRequest request = new DocumentRequest("1", longContent.toString());
        when(indexingService.index(any(DocumentRequest.class))).thenReturn(0);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(indexingService, times(1)).index(any(DocumentRequest.class));
    }

    @Test
    void testSearchReturnsCompleteSearchResult() throws Exception {
        SearchResult result = new SearchResult(5, "Full document content here", 0.87);
        SearchResponse response = new SearchResponse(List.of(result));

        when(searchService.search(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].documentId").value(5))
                .andExpect(jsonPath("$.results[0].content").value("Full document content here"))
                .andExpect(jsonPath("$.results[0].score").value(0.87));
    }
}

