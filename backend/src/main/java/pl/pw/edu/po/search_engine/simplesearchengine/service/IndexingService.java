package pl.pw.edu.po.search_engine.simplesearchengine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.PolishTextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextProcessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.SearchIndex;

import java.util.List;

/**
 * Service for document indexing operations.
 * Uses constructor injection for all dependencies (proper Spring DI).
 * All dependencies are thread-safe singletons managed by Spring container.
 */
@Service
@Slf4j
public class IndexingService {

    private final TextPreprocessor englishPreprocessor;
    private final PolishTextPreprocessor polishTextPreprocessor;
    private final SearchIndex searchIndex;

    /**
     * Constructor injection - all dependencies provided by Spring.
     * @param englishPreprocessor English text processor bean
     * @param polishTextPreprocessor Polish text processor bean
     * @param searchIndex Thread-safe search index bean
     */
    public IndexingService(
            TextPreprocessor englishPreprocessor,
            PolishTextPreprocessor polishTextPreprocessor,
            SearchIndex searchIndex) {
        this.englishPreprocessor = englishPreprocessor;
        this.polishTextPreprocessor = polishTextPreprocessor;
        this.searchIndex = searchIndex;
    }

    /**
     * Select text preprocessor based on language.
     * Defaults to Polish if language not specified.
     */
    private TextProcessor selectPreprocessor(String language) {
        if ("en".equalsIgnoreCase(language)) {
            return englishPreprocessor;
        }
        return polishTextPreprocessor;
    }

    /**
     * Indexing new document (stara metoda - kompatybilność wsteczna)
     * 1. Processing text (tokenizing, stopwords, stemming)
     * 2. Save result to index
     */
    public int index(DocumentRequest request) {
        String content = request.getContent();
        TextProcessor processor = selectPreprocessor(request.getLanguage());
        List<String> tokens = processor.process(content);
        return searchIndex.addDocument(content, tokens);
    }

    /**
     * Add document to index (używana przez DocumentService)
     * @param docId - Document ID from PostgreSQL
     * @param content - Document content
     */
    public void addDocument(String docId, String content) {
        log.debug("Adding document to index: docId={}", docId);
        TextProcessor processor = selectPreprocessor("pl");
        List<String> tokens = processor.process(content);
        searchIndex.addDocument(Integer.parseInt(docId), content, tokens);
    }

    /**
     * Remove document from index
     * @param docId - Document ID to remove
     */
    public void removeDocument(String docId) {
        log.debug("Removing document from index: docId={}", docId);
        searchIndex.removeDocument(Integer.parseInt(docId));
    }

    /**
     * Clear entire index (remove all documents)
     */
    public void clearIndex() {
        log.info("Clearing entire index");
        searchIndex.clear();
    }

    /**
     * Returns number of all indexed documents
     */
    public int getDocumentCount() {
        return searchIndex.getDocumentCount();
    }

    /**
     * Helper function for tests
     */
    public void printIndex() {
        // Note: This method requires access to internal structure
        // Consider removing or reimplementing if needed
        log.warn("printIndex() is deprecated - index internals are now encapsulated");
    }

    /**
     * Replace index content with new index (delegation pattern)
     * Clears current index and merges content from newIndex
     * Keeps the same SearchIndex object instance (final field)
     */
    public void replaceIndex(SearchIndex newIndex) {
        searchIndex.clear();
        searchIndex.merge(newIndex);
    }
}
