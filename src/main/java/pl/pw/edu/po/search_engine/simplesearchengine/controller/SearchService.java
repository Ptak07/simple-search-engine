package pl.pw.edu.po.search_engine.simplesearchengine.controller;

import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResponse;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.SearchResult;

import java.util.List;

@Service
public class SearchService {

    // Temporary placeholder login
    public void indexDocument(DocumentRequest documentRequest) {
        System.out.println("📄 Indexing document:");
        System.out.println("ID: " + documentRequest.getId());
        System.out.println("Content: " + documentRequest.getContent());
    }

    public SearchResponse search(String query) {
        System.out.println("🔍 Searching for: " + query);
        // Return a fake search result for now
        return new SearchResponse(List.of(new SearchResult("doc1", 0.95)));
    }
}
