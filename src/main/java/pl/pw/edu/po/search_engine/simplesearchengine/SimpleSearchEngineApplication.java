package pl.pw.edu.po.search_engine.simplesearchengine;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.analysis.TextPreprocessor;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.InvertedIndex;
import pl.pw.edu.po.search_engine.simplesearchengine.service.IndexingService;

import java.util.List;

@SpringBootApplication
public class SimpleSearchEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleSearchEngineApplication.class, args);
	}

}
