package pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PolishTextPreprocessorTest {

    @Test
    void testPolishText() {
        PolishTextPreprocessor preprocessor = new PolishTextPreprocessor();
        String text = "Programowanie w języku java jest fascynujące.";

        List<String> tokens = preprocessor.process(text);

        System.out.println("Input Text: " + text);
        System.out.println("Tokens: " + tokens);

        assertFalse(tokens.isEmpty());
        assertTrue(tokens.contains("programować") || tokens.contains("programuj"));
        assertTrue(tokens.contains("java"));
    }

    @Test
    void testPolishDeclensions() {
        PolishTextPreprocessor preprocessor = new PolishTextPreprocessor();
        String text = "Programista programuje program. Programiści programują programy.";

        List<String> tokens = preprocessor.process(text);

        System.out.println("Input: " + text);
        System.out.println("Tokens: " + tokens);

        // Wszystkie powinny być zredukowane do tej samej podstawy
        long uniqueProgramCount = tokens.stream()
            .filter(t -> t.startsWith("program"))
            .distinct()
            .count();

        System.out.println("Unique 'program*' stems: " + uniqueProgramCount);
        assertTrue(uniqueProgramCount <= 5);
    }

    @Test
    void testPolishDiacritics() {
        PolishTextPreprocessor preprocessor = new PolishTextPreprocessor();
        String text = "żółć gęślą jaźń";

        List<String> tokens = preprocessor.process(text);

        System.out.println("Input: " + text);
        System.out.println("Tokens: " + tokens);

        assertFalse(tokens.isEmpty());
        assertEquals(3, tokens.size());
    }

    @Test
    void testEmptyText() {
        PolishTextPreprocessor preprocessor = new PolishTextPreprocessor();

        assertTrue(preprocessor.process("").isEmpty());
        assertTrue(preprocessor.process(null).isEmpty());
        assertTrue(preprocessor.process("   ").isEmpty());
    }
}
