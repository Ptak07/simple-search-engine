package pl.pw.edu.po.search_engine.simplesearchengine.exception;

public class DuplicateUrlException extends RuntimeException {
    public DuplicateUrlException(String message) {
        super(message);
    }
}
