
package pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PolishTextPreprocessor {

    private final PolishAnalyzer analyzer;

    public PolishTextPreprocessor() {
        this.analyzer = new PolishAnalyzer();
    }

    public List<String> process(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> tokens = new ArrayList<>();
        try {
            TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                String token = termAttribute.toString();
                tokens.add(token);
            }
            tokenStream.end();
        } catch (Exception e) {
            throw new RuntimeException("Error processing Polish text", e);
        }

        return tokens;
    }
}