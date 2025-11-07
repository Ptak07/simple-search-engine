package pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis;

import org.tartarus.snowball.ext.EnglishStemmer;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextPreprocessor {

    // List of common English stop words
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it", "no", "not",
            "of", "on", "or", "such", "that", "the", "their", "then",
            "there", "these", "they", "this", "to", "was", "will", "with"
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");
    private final ThreadLocal<EnglishStemmer> stemmer = ThreadLocal.withInitial(EnglishStemmer::new);

    public List<String> process(String text) {
        if (text == null || text.isBlank()) return List.of();

        // Lowercase
        String lower = text.toLowerCase(Locale.ROOT);

        // Tokenizing
        List<String> tokens = Arrays.stream(TOKEN_PATTERN.split(lower))
                .filter(token -> !token.isBlank())
                .toList();

        // Removing stop words
        List<String> filtered = tokens.stream()
                .filter(token -> !STOP_WORDS.contains(token))
                .toList();

        // Stemming
        return filtered.stream()
                .map(this::stem)
                .collect(Collectors.toList());
    }

    private String stem(String word) {
        EnglishStemmer stemmerInstance = stemmer.get();
        stemmerInstance.setCurrent(word);
        if (stemmerInstance.stem()) {
            return stemmerInstance.getCurrent();
        }
        return word;
    }
}