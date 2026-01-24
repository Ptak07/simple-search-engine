package pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis;

import java.util.List;

public interface TextProcessor {
    List<String> process(String text);
}