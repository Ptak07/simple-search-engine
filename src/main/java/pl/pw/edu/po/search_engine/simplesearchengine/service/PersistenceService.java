package pl.pw.edu.po.search_engine.simplesearchengine.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import pl.pw.edu.po.search_engine.simplesearchengine.engine.core.InvertedIndex;

import java.io.*;

/**
 * Handles saving and loading of the inverted index to disk.
 */
@Service
public class PersistenceService {

    private static final String INDEX_FILE = "index.ser";
    private final IndexingService indexingService;

    public PersistenceService(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    /** Load existing index on application startup. */
    @PostConstruct
    public void loadIndex() {
        // Register shutdown hook for SIGINT (Ctrl+C) handling
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Persistence] Shutdown hook triggered. Saving index...");
            saveIndex();
        }));

        File file = new File(INDEX_FILE);
        if (!file.exists()) {
            System.out.println("[Persistence] No index file found. Starting fresh.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            InvertedIndex loaded = (InvertedIndex) ois.readObject();
            indexingService.replaceIndex(loaded);
            System.out.println("[Persistence] Index loaded successfully.");
        } catch (Exception e) {
            System.err.println("[Persistence] Failed to load index: " + e.getMessage());
        }
    }

    /** Save index when application stops. */
    @PreDestroy
    public void saveIndex() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INDEX_FILE))) {
            oos.writeObject(indexingService.getInvertedIndex());
            System.out.println("[Persistence] Index saved successfully.");
        } catch (IOException e) {
            System.err.println("[Persistence] Failed to save index: " + e.getMessage());
        }
    }
}
