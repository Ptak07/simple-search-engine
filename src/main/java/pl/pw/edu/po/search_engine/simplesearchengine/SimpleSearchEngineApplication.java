package pl.pw.edu.po.search_engine.simplesearchengine;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.pw.edu.po.search_engine.simplesearchengine.dto.DocumentRequest;
import pl.pw.edu.po.search_engine.simplesearchengine.model.Document;
import pl.pw.edu.po.search_engine.simplesearchengine.service.DocumentService;

import java.util.List;

@SpringBootApplication
public class SimpleSearchEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleSearchEngineApplication.class, args);
	}

    @Bean
    CommandLineRunner testDocumentService(DocumentService documentService) {
        return args -> {
            System.out.println("\n🎯 === TESTOWANIE DOCUMENT SERVICE ===\n");

            // 1. Wyczyść bazę
            documentService.deleteAllDocuments();
            System.out.println("✅ Wyczyszczono bazę");

            // 2. Dodaj dokumenty
            System.out.println("\n📝 Dodawanie dokumentów...");

            DocumentRequest doc1 = new DocumentRequest();
            doc1.setTitle("Machine Learning Basics");
            doc1.setContent("Machine learning is a subset of artificial intelligence that focuses on algorithms");
            doc1.setUrl("https://example.com/ml-basics");

            DocumentRequest doc2 = new DocumentRequest();
            doc2.setTitle("Deep Learning Guide");
            doc2.setContent("Deep learning uses neural networks with multiple layers to learn from data");
            doc2.setUrl("https://example.com/deep-learning");

            Document saved1 = documentService.addDocument(doc1);
            Document saved2 = documentService.addDocument(doc2);

            System.out.println("✅ Dodano: " + saved1.getTitle() + " (ID=" + saved1.getId() + ")");
            System.out.println("✅ Dodano: " + saved2.getTitle() + " (ID=" + saved2.getId() + ")");

            // 3. Test duplikatu
            System.out.println("\n🔄 Test duplikatu URL...");
            try {
                documentService.addDocument(doc1);
                System.out.println("❌ BŁĄD: Duplikat nie został wykryty!");
            } catch (Exception e) {
                System.out.println("✅ OK: " + e.getMessage());
            }

            // 4. Pobierz wszystkie
            System.out.println("\n📚 Wszystkie dokumenty:");
            List<Document> all = documentService.getAllDocuments();
            all.forEach(d -> System.out.println("  - [" + d.getId() + "] " + d.getTitle()));

            // 5. Pobierz po ID (użyj saved1.getId() zamiast 1L)
            System.out.println("\n🔍 Pobierz dokument ID=" + saved1.getId() + ":");
            Document byId = documentService.getDocumentById(saved1.getId());
            System.out.println("  ✅ " + byId.getTitle());

            // 6. Pobierz po URL
            System.out.println("\n🔍 Pobierz po URL:");
            Document byUrl = documentService.getDocumentByUrl("https://example.com/ml-basics");
            System.out.println("  ✅ " + byUrl.getTitle());

            // 7. Aktualizuj (użyj saved1.getId())
            System.out.println("\n✏️ Aktualizacja dokumentu ID=" + saved1.getId() + "...");
            DocumentRequest updated = new DocumentRequest();
            updated.setTitle("Machine Learning UPDATED");
            updated.setContent("Updated content about machine learning");
            updated.setUrl("https://example.com/ml-basics");

            Document updatedDoc = documentService.updateDocument(saved1.getId(), updated);
            System.out.println("  ✅ Zaktualizowano: " + updatedDoc.getTitle());

            // 8. Usuń (użyj saved2.getId())
            System.out.println("\n🗑️ Usuwanie dokumentu ID=" + saved2.getId() + "...");
            documentService.deleteDocument(saved2.getId());
            System.out.println("  ✅ Usunięto");

            // 9. Liczba dokumentów
            long count = documentService.countDocuments();
            System.out.println("\n📊 Liczba dokumentów: " + count);

            System.out.println("\n🎉 === TEST ZAKOŃCZONY ===\n");
        };
    }
}
