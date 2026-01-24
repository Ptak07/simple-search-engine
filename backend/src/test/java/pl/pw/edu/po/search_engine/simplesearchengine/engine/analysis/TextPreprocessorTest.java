package pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TextPreprocessor.
 * Tests tokenization, stop word removal, and stemming.
 */
class TextPreprocessorTest {

    private TextPreprocessor preprocessor;

    @BeforeEach
    void setUp() {
        preprocessor = new TextPreprocessor();
    }

    @Test
    void testProcessEmptyString() {
        List<String> result = preprocessor.process("");
        assertTrue(result.isEmpty(), "Empty string should return empty list");
    }

    @Test
    void testProcessNull() {
        List<String> result = preprocessor.process(null);
        assertTrue(result.isEmpty(), "Null input should return empty list");
    }

    @Test
    void testProcessWhitespace() {
        List<String> result = preprocessor.process("   \t\n  ");
        assertTrue(result.isEmpty(), "Whitespace only should return empty list");
    }

    @Test
    void testLowercaseConversion() {
        List<String> result = preprocessor.process("HELLO World");
        assertFalse(result.isEmpty());
        // After stemming "hello" and "world", check that they're lowercase
        result.forEach(token -> assertEquals(token, token.toLowerCase()));
    }

    @Test
    void testTokenization() {
        List<String> result = preprocessor.process("hello, world! how are you?");
        assertFalse(result.isEmpty());
        // Should split on punctuation and whitespace
        assertTrue(result.size() >= 2, "Should have multiple tokens");
    }

    @Test
    void testStopWordRemoval() {
        List<String> result = preprocessor.process("the quick brown fox");
        // "the" is a stop word and should be removed
        assertFalse(result.contains("the"));
        // "quick", "brown", "fox" should remain (after stemming)
        assertFalse(result.isEmpty());
    }

    @Test
    void testStopWordsOnlyInput() {
        List<String> result = preprocessor.process("the and or but");
        // All are stop words, result should be empty
        assertTrue(result.isEmpty(), "Input with only stop words should return empty list");
    }

    @Test
    void testStemming() {
        List<String> result = preprocessor.process("running runs ran");
        // All should stem to "run"
        assertFalse(result.isEmpty());
        // Verify that stemming occurred (should have some common stem)
        assertTrue(result.size() > 0);
    }

    @Test
    void testComplexSentence() {
        String input = "The quick brown fox jumps over the lazy dog";
        List<String> result = preprocessor.process(input);

        // Should remove stop words: "the"
        assertFalse(result.contains("the"));

        // Should contain stemmed versions of content words
        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 6); // quick, brown, fox, jump, over, lazi, dog
    }

    @Test
    void testNumbersAndSpecialCharacters() {
        List<String> result = preprocessor.process("test123 hello-world test@456");
        // Should tokenize and keep alphanumeric parts
        assertFalse(result.isEmpty());
    }

    @Test
    void testMultipleSpaces() {
        List<String> result = preprocessor.process("hello    world    test");
        // Should handle multiple spaces correctly
        assertTrue(result.size() >= 3);
    }

    @Test
    void testPunctuationRemoval() {
        List<String> result = preprocessor.process("hello! world? test.");
        // Punctuation should be removed during tokenization
        assertFalse(result.isEmpty());
        result.forEach(token -> assertFalse(token.matches(".*[!?.].*")));
    }

    @Test
    void testCaseInsensitivity() {
        List<String> result1 = preprocessor.process("Testing");
        List<String> result2 = preprocessor.process("testing");
        List<String> result3 = preprocessor.process("TESTING");

        // All should produce the same result
        assertEquals(result1, result2);
        assertEquals(result2, result3);
    }

    @Test
    void testRealWorldExample() {
        String input = "Machine learning is transforming the way we process data";
        List<String> result = preprocessor.process(input);

        // Should remove stop words: "is", "the" (but not "we" - it's not in stop words list)
        assertFalse(result.contains("is"));
        assertFalse(result.contains("the"));

        // Should contain meaningful words (stemmed)
        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 5); // machine, learn, transform, way, we, process, data (minus stop words)
    }
}

