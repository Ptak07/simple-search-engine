package pl.pw.edu.po.search_engine.simplesearchengine.service;

import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.InvertedIndex;

import java.util.List;

@Service
public class IndexingService {

    private final TextPreprocessor textPreprocessor;
    private final InvertedIndex invertedIndex;

    public IndexingService() {
        this.textPreprocessor = new TextPreprocessor();
        this.invertedIndex = new InvertedIndex();
    }

    /**
     * Indexing new document
     * 1. Processing text (tokenizing, stopwords, stemming)
     * 2. Save result to index
     */
    public int index(DocumentRequest request) {
        String content = request.getContent();
        List<String> tokens = textPreprocessor.process(content);
        return invertedIndex.addDocument(content, tokens);
    }

    /**
     * Returns number of all indexed documents
     */
    public int getDocumentCount() {
        return invertedIndex.getDocumentCount();
    }

    /**
     * Heler function for tests
     */
    public void printIndex() {
        invertedIndex.printIndex();
    }

    public InvertedIndex getInvertedIndex() {
        return invertedIndex;
    }
}
