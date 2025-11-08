package pl.pw.edu.po.search_engine.simplesearchengine.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.InvertedIndex;

import java.util.List;

@Service
@Slf4j
public class IndexingService {

    private final TextPreprocessor textPreprocessor;
    @Getter
    private final InvertedIndex invertedIndex;

    public IndexingService() {
        this.textPreprocessor = new TextPreprocessor();
        this.invertedIndex = new InvertedIndex();
    }

    /**
     * Indexing new document (stara metoda - kompatybilność wsteczna)
     * 1. Processing text (tokenizing, stopwords, stemming)
     * 2. Save result to index
     */
    public int index(DocumentRequest request) {
        String content = request.getContent();
        List<String> tokens = textPreprocessor.process(content);
        return invertedIndex.addDocument(content, tokens);
    }

    /**
     * Add document to index (używana przez DocumentService)
     * @param docId - Document ID from PostgreSQL
     * @param content - Document content
     */
    public void addDocument(String docId, String content) {
        log.debug("Adding document to index: docId={}", docId);
        List<String> tokens = textPreprocessor.process(content);
        invertedIndex.addDocument(content, tokens);
    }

    /**
     * Remove document from index
     * @param docId - Document ID to remove
     */
    public void removeDocument(String docId) {
        log.debug("Removing document from index: docId={}", docId);
        invertedIndex.removeDocument(Integer.parseInt(docId));
    }

    /**
     * Clear entire index (remove all documents)
     */
    public void clearIndex() {
        log.info("Clearing entire index");
        invertedIndex.clear();
    }

    /**
     * Returns number of all indexed documents
     */
    public int getDocumentCount() {
        return invertedIndex.getDocumentCount();
    }

    /**
     * Helper function for tests
     */
    public void printIndex() {
        invertedIndex.printIndex();
    }

    /**
     * Replace index content with new index (delegation pattern)
     * Clears current index and merges content from newIndex
     * Keeps the same InvertedIndex object instance (final field)
     */
    public void replaceIndex(InvertedIndex newIndex) {
        invertedIndex.clear();
        invertedIndex.merge(newIndex);
    }
}
