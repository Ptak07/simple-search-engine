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

    @Bean
    CommandLineRunner demoLocal() {
        return args -> {
            var preprocessor = new TextPreprocessor();
            var index = new InvertedIndex();

            List<String> tokens1 = preprocessor.process("Java Spring Boot tutorial");
            List<String> tokens2 = preprocessor.process("Learn Java programming");
            List<String> tokens3 = preprocessor.process("Spring Boot guide");

            index.addDocument("Java Spring Boot tutorial", tokens1);
            index.addDocument("Learn Java programming", tokens2);
            index.addDocument("Spring Boot guide", tokens3);

            System.out.println("=== INVERTED INDEX ===");
            index.printIndex();

            System.out.println("\nDocuments containing 'spring': " + index.getDocumentsForTerm("spring"));
            System.out.println("Documents containing 'java': " + index.getDocumentsForTerm("java"));
        };
    }

    @Bean
    CommandLineRunner demoIndexingService(IndexingService indexingService) {
        return args -> {
            var doc1 = new DocumentRequest("doc1", "Java Spring Boot tutorial");
            var doc2 = new DocumentRequest("doc2", "Learn Java programming");
            var doc3 = new DocumentRequest("doc3", "Spring Boot guide");

            indexingService.index(doc1);
            indexingService.index(doc2);
            indexingService.index(doc3);

            System.out.println("Indexed documents: " + indexingService.getDocumentCount());
            indexingService.printIndex();
        };
    }
}
