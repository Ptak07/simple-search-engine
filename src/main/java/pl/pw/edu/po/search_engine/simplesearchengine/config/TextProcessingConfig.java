package pl.pw.edu.po.search_engine.simplesearchengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.PolishTextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.InvertedIndex;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.SearchIndex;

/**
 * Spring configuration for text processing and indexing components.
 * Provides singleton beans for dependency injection instead of manual instantiation.
 */
@Configuration
public class TextProcessingConfig {

    /**
     * English text preprocessor bean.
     * Singleton - shared across all services.
     */
    @Bean
    public TextPreprocessor englishTextPreprocessor() {
        return new TextPreprocessor();
    }

    /**
     * Polish text preprocessor bean.
     * Singleton - shared across all services.
     */
    @Bean
    public PolishTextPreprocessor polishTextPreprocessor() {
        return new PolishTextPreprocessor();
    }

    /**
     * Search index bean.
     * Thread-safe singleton - shared across all services.
     * This is the single source of truth for the in-memory index.
     */
    @Bean
    public SearchIndex searchIndex() {
        return new InvertedIndex();
    }
}

