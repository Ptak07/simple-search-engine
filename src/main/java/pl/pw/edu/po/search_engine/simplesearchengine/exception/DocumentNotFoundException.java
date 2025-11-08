package pl.pw.edu.po.search_engine.simplesearchengine.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String message) {
        super(message);
    }
}
