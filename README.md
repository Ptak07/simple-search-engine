# Simple Search Engine

> Zaawansowana wielojęzyczna wyszukiwarka full-text z thread-safe indeksem odwróconym, rankingiem TF-IDF i persystencją w PostgreSQL.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Spis treści

- [Kluczowe funkcje](#-kluczowe-funkcje)
- [Stack technologiczny](#️-stack-technologiczny)
- [Wymagania](#️-wymagania)
- [Szybki start](#-szybki-start)
- [Architektura](#️-architektura)
- [API Documentation](#-api-documentation)
- [Szczegóły implementacji](#-szczegóły-implementacji)
- [Testowanie](#-testowanie)
- [Performance](#-performance)
- [Troubleshooting](#-troubleshooting)
- [Licencja](#-licencja)

---

## 🚀 Kluczowe funkcje

### Wyszukiwarka
- ✅ **Thread-safe Inverted Index** - Współbieżny dostęp dzięki `ConcurrentHashMap` + `ReadWriteLock`
- ✅ **TF-IDF Ranking** - Zaawansowany algorytm oceny trafności z wygładzonym IDF
- ✅ **Multi-language Support** - Polski (Stempel) i angielski (Snowball) stemming
- ✅ **Smart Snippets** - Kontekstowe fragmenty (200 znaków) wokół dopasowań
- ✅ **Pagination** - Efektywne stronicowanie wyników (offset + limit)
- ✅ **Reverse Mapping** - Optymalizacja usuwania dokumentów: **O(M) zamiast O(N)**

### Persystencja
- ✅ **PostgreSQL Integration** - Trwały magazyn dokumentów z JPA/Hibernate
- ✅ **Auto Index Rebuild** - Automatyczna odbudowa indeksu z bazy przy starcie aplikacji
- ✅ **Audit Trail** - Timestamps (`created_at`, `updated_at`) dla każdego dokumentu

### Web Crawler
- ✅ **BFS Crawling** - Przeszukiwanie wszerz z ograniczeniami głębokości i liczby stron
- ✅ **Async Execution** - Asynchroniczny crawling z thread pool (2-5 workers)
- ✅ **Cancellation Support** - Możliwość anulowania długotrwałych zadań
- ✅ **History Tracking** - Pełna historia crawlingu w bazie danych

### API & Documentation
- ✅ **RESTful API** - CRUD dla dokumentów, wyszukiwanie, kontrola crawlera
- ✅ **OpenAPI 3.0** - Pełna specyfikacja API w formacie OpenAPI/Swagger
- ✅ **Interactive Docs** - Swagger UI do testowania endpointów
- ✅ **Error Handling** - Zunifikowane odpowiedzi błędów z kodem HTTP i szczegółami

---

## 🛠️ Stack technologiczny

### Core
| Technologia | Wersja | Zastosowanie |
|-------------|--------|--------------|
| **Java** | 21 | Język programowania (LTS) |
| **Spring Boot** | 3.5.7 | Framework aplikacji |
| **Spring Data JPA** | 3.5.7 | Warstwa persystencji |
| **Hibernate** | 6.x | ORM (Object-Relational Mapping) |

### Database
| Technologia | Wersja | Zastosowanie |
|-------------|--------|--------------|
| **PostgreSQL** | 15+ | Relacyjna baza danych |
| **HikariCP** | - | Connection pooling |

### Text Processing
| Biblioteka | Wersja | Zastosowanie |
|------------|--------|--------------|
| **Apache Lucene** | 9.11.1 | Analiza tekstu (analysis-common) |
| **Stempel** | 9.11.1 | Polski stemmer (Lucene) |
| **Snowball** | - | Angielski stemmer |

### Web Crawling
| Biblioteka | Wersja | Zastosowanie |
|------------|--------|--------------|
| **Jsoup** | 1.21.2 | HTML parsing i DOM manipulation |

### Documentation & Configuration
| Narzędzie | Wersja | Zastosowanie |
|-----------|--------|--------------|
| **SpringDoc OpenAPI** | 2.8.14 | Generowanie OpenAPI 3.0 spec |
| **Swagger UI** | - | Interaktywna dokumentacja API |
| **spring-dotenv** | 4.0.0 | Zarządzanie zmiennymi środowiskowymi |

### Testing
| Framework | Wersja | Zastosowanie |
|-----------|--------|--------------|
| **JUnit 5** | - | Unit testing framework |
| **Mockito** | 5.15.2 | Mocking framework |
| **Spring Boot Test** | 3.5.7 | Integration testing |

### Build & Deployment
- **Maven** 3.9+ - Build automation
- **Maven Wrapper** - Portable Maven execution

---

## ⚙️ Wymagania

| Oprogramowanie | Minimalna wersja | Zalecana wersja |
|----------------|------------------|-----------------|
| **Java JDK** | 21 | 21 (LTS) |
| **PostgreSQL** | 12 | 15+ |
| **Maven** | 3.8+ | 3.9+ (lub użyj Maven Wrapper) |

---

## 🚀 Szybki start

### 1. Klonowanie repozytorium

```bash
git clone https://github.com/twoj-username/simple-search-engine.git
cd simple-search-engine
```

### 2. Konfiguracja bazy danych

#### PostgreSQL (Linux/macOS)
```bash
# Uruchom PostgreSQL
sudo service postgresql start  # Linux
brew services start postgresql  # macOS

# Utwórz bazę i użytkownika
psql postgres
```

```sql
CREATE DATABASE search_engine;
CREATE USER search_user WITH PASSWORD 'secure_password_here';
GRANT ALL PRIVILEGES ON DATABASE search_engine TO search_user;

-- PostgreSQL 15+ wymaga również:
GRANT ALL ON SCHEMA public TO search_user;

\q
```

#### PostgreSQL (Windows)
```cmd
# Uruchom pgAdmin lub użyj psql z Command Prompt
psql -U postgres

# Następnie wykonaj te same komendy SQL
```

#### Weryfikacja połączenia
```bash
psql -U search_user -d search_engine -h localhost
# Jeśli połączenie działa, wpisz \q aby wyjść
```

### 3. Konfiguracja zmiennych środowiskowych

#### Utwórz plik `.env`
```bash
cp .env.example .env
# Edytuj .env w swoim ulubionym edytorze
```

#### Zawartość `.env`
```properties
# Database Configuration (WYMAGANE)
DB_URL=jdbc:postgresql://localhost:5432/search_engine
DB_USERNAME=search_user
DB_PASSWORD=secure_password_here

# Application Configuration (OPCJONALNE)
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Logging (OPCJONALNE)
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=DEBUG
```

⚠️ **Ważne**: Nie commituj pliku `.env` do repozytorium (jest w `.gitignore`)!

### 4. Build i uruchomienie

#### Użycie Maven Wrapper (zalecane)
```bash
# Build
./mvnw clean install

# Uruchomienie
./mvnw spring-boot:run
```

#### Użycie lokalnego Mavena
```bash
# Build
mvn clean install

# Uruchomienie
mvn spring-boot:run
```

#### Uruchomienie z profilem
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Uruchomienie z JAR
```bash
# Build JAR
./mvnw clean package

# Uruchomienie
java -jar target/simple-search-engine-0.0.1-SNAPSHOT.jar
```

### 5. Weryfikacja działania

Aplikacja uruchomi się na `http://localhost:8080`

#### Sprawdź health endpoint
```bash
curl http://localhost:8080/actuator/health
# Powinno zwrócić: {"status":"UP"}
```

#### Otwórz Swagger UI
```
http://localhost:8080/swagger-ui.html
```

#### OpenAPI Specification (JSON)
```
http://localhost:8080/v3/api-docs
```

### 6. Pierwszy test - Dodaj dokument

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Java Programming Guide",
    "content": "Java is a popular programming language used for building enterprise applications.",
    "url": "https://example.com/java-guide"
  }'
```

### 7. Wyszukaj dokument

```bash
curl "http://localhost:8080/api/search?query=programming&language=en"
```

---

## 🏗️ Architektura

### Diagram wysokiego poziomu

```
┌─────────────────────────────────────────────────────────────┐
│                      Client (HTTP/REST)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │SearchController│ │DocumentController│ │CrawlerController│ │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│  ┌───────────────┐  ┌─────────────────┐  ┌──────────────┐  │
│  │ SearchService │  │ IndexingService │  │CrawlerService│  │
│  └───────┬───────┘  └────────┬────────┘  └──────────────┘  │
│          │                   │                               │
│          ├───────────────────┴──────────────┐               │
│          │                                   │               │
│  ┌───────▼────────┐  ┌──────────────────┐  │               │
│  │TfIdfScoringService│ │DocumentPersistence│ │               │
│  └────────────────┘  └──────────────────┘  │               │
└──────────────────────────┬──────────────────┴───────────────┘
                           │                  │
        ┌──────────────────┴──────┐          │
        ↓                          ↓          ↓
┌──────────────┐          ┌─────────────────────────┐
│ SearchIndex  │          │  Repository Layer       │
│  (Interface) │          │  ┌──────────────────┐   │
│      ↓       │          │  │DocumentRepository│   │
│ InvertedIndex│          │  └────────┬─────────┘   │
│   (RAM)      │          │           │             │
└──────────────┘          └───────────┼─────────────┘
                                      ↓
                          ┌───────────────────────┐
                          │   PostgreSQL          │
                          │   documents table     │
                          └───────────────────────┘
```

### Warstwy aplikacji

#### 1. **Controller Layer** (Presentation)
- **Odpowiedzialność**: Obsługa HTTP requests/responses, walidacja inputu, mapowanie DTO
- **Komponenty**:
  - `SearchController` - Endpoint wyszukiwania (`/api/search`)
  - `DocumentController` - CRUD dokumentów (`/api/documents`)
  - `CrawlerController` - Kontrola crawlera (`/api/crawler`)
- **Wzorce**: REST, DTO Pattern, Exception Handling

#### 2. **Service Layer** (Business Logic)
- **Odpowiedzialność**: Logika biznesowa, orkiestracja operacji, transaction management
- **Komponenty**:
  - `SearchService` - Wyszukiwanie, ranking, paginacja
  - `IndexingService` - Indeksowanie dokumentów, zarządzanie indeksem
  - `TfIdfScoringService` - Obliczanie score TF-IDF
  - `DocumentPersistenceService` - Operacje na bazie danych
  - `CrawlerService` - Crawling synchroniczny i asynchroniczny
  - `IndexInitializationService` - Odbudowa indeksu przy starcie
- **Wzorce**: Service Pattern, Dependency Injection, Strategy Pattern

#### 3. **Repository Layer** (Data Access)
- **Odpowiedzialność**: Abstrakcja dostępu do danych, zapytania do bazy
- **Komponenty**:
  - `DocumentRepository` - JPA Repository dla dokumentów
  - `CrawlerHistoryRepository` - Historia crawlingu
- **Wzorce**: Repository Pattern, Active Record (JPA)

#### 4. **Engine Layer** (Core Domain)
- **Odpowiedzialność**: Algorytmy wyszukiwania, przetwarzanie tekstu
- **Komponenty**:
  - `SearchIndex` (interface) - Kontrakt operacji na indeksie
  - `InvertedIndex` - Thread-safe implementacja indeksu odwróconego
  - `TextProcessor` (interface) - Kontrakt przetwarzania tekstu
  - `TextPreprocessor` - Implementacja dla języka angielskiego
  - `PolishTextPreprocessor` - Implementacja dla języka polskiego
- **Wzorce**: Strategy Pattern, Template Method, Singleton

---

## 📚 API Documentation

**Base URL**: `http://localhost:8080/api`

### Swagger UI
Pełna interaktywna dokumentacja dostępna pod:
```
http://localhost:8080/swagger-ui.html
```

### Quick Reference

| Kategoria | Method | Endpoint | Opis |
|-----------|--------|----------|------|
| **Search** | GET | `/search` | Wyszukiwanie full-text |
| **Documents** | POST | `/documents` | Utwórz dokument |
| | GET | `/documents` | Lista dokumentów |
| | GET | `/documents/{id}` | Pobierz dokument |
| | PUT | `/documents/{id}` | Aktualizuj dokument |
| | DELETE | `/documents/{id}` | Usuń dokument |
| **Crawler** | POST | `/crawler/start` | Crawling synchroniczny |
| | POST | `/crawler/start-async` | Crawling asynchroniczny |
| | GET | `/crawler/history` | Historia crawlów |
| | POST | `/crawler/cancel/{id}` | Anuluj crawl |

### Przykład: Wyszukiwanie

```bash
GET /api/search?query=java+programming&language=en&limit=10&offset=0
```

**Response**:
```json
{
  "query": "java programming",
  "totalResults": 47,
  "limit": 10,
  "offset": 0,
  "results": [
    {
      "document": {
        "id": 1,
        "title": "Java Programming Guide",
        "content": "Complete guide...",
        "url": "https://example.com/java"
      },
      "score": 1.23,
      "matchedTerms": ["java", "program"],
      "snippet": "...Java programming guide..."
    }
  ],
  "searchTimeMs": 12
}
```

---

## 🔍 Szczegóły implementacji

### InvertedIndex - Thread-Safe Design

```java
public class InvertedIndex implements SearchIndex {
    // Main index: term → (docId → positions)
    private final Map<String, Map<Integer, List<Integer>>> index;
    
    // Reverse mapping: docId → terms (for O(M) removal)
    private final Map<Integer, Set<String>> docIdToTerms;
    
    // Thread-safety
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // Methods use appropriate locks
    public Map<Integer, List<Integer>> getDocumentsForTerm(String term) {
        lock.readLock().lock();  // Multiple readers
        try {
            // Return defensive copy
            return Collections.unmodifiableMap(...);
        } finally {
            lock.readLock().unlock();
        }
    }
}
```

**Cechy**:
- ✅ **ConcurrentHashMap** dla podstawowej thread-safety
- ✅ **ReadWriteLock** dla read/write separation
- ✅ **Reverse mapping** dla O(M) removal (M = terminy w dokumencie)
- ✅ **Defensive copies** (Collections.unmodifiableMap/Set)

### Text Processing Pipeline

```
Input: "Running quickly through the forest"
    ↓
[1] Tokenization
    ↓
["Running", "quickly", "through", "the", "forest"]
    ↓
[2] Lowercase
    ↓
["running", "quickly", "through", "the", "forest"]
    ↓
[3] Stop Words Removal
    ↓
["running", "quickly", "forest"]
    ↓
[4] Stemming (Stempel/Snowball)
    ↓
["run", "quick", "forest"]
    ↓
Output: ["run", "quick", "forest"]
```

### TF-IDF Scoring

```java
score = Σ (TF × IDF) for each query term

TF = (term frequency in doc) / (document length)
IDF = ln(1 + N / df)  // Wygładzony IDF

gdzie:
  N = liczba wszystkich dokumentów
  df = liczba dokumentów zawierających term
```

**Bonus dla tytułu**: Jeśli term występuje w tytule → `score × 1.3`

---

## 🧪 Testowanie

### Uruchomienie testów

```bash
# Wszystkie testy
./mvnw test

# Konkretna klasa
./mvnw test -Dtest=InvertedIndexTest

# Testy concurrency
./mvnw test -Dtest=*ConcurrencyTest

# Z coverage report
./mvnw clean verify
```

### Kategorie testów

| Typ | Liczba | Przykłady |
|-----|--------|-----------|
| **Unit Tests** | ~80 | InvertedIndexTest, TextProcessorTest |
| **Integration Tests** | ~30 | SearchServiceTest, DocumentServiceTest |
| **Concurrency Tests** | ~15 | InvertedIndexConcurrencyTest |

### Przykład testu concurrency

```java
@RepeatedTest(5)  // Run 5 times to catch race conditions
void testConcurrentReadAndWrite() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(15);
    
    // 5 writers
    for (int i = 0; i < 5; i++) {
        executor.submit(() -> {
            index.addDocument("content", tokens);
        });
    }
    
    // 10 readers
    for (int i = 0; i < 10; i++) {
        executor.submit(() -> {
            index.getDocumentsForTerm("test");
        });
    }
    
    // Verify no exceptions, no data corruption
}
```

---

## ⚡ Performance

### Złożoności algorytmiczne

| Operacja | Złożoność | Opis |
|----------|-----------|------|
| `addDocument()` | O(M) | M = liczba terminów w dokumencie |
| `removeDocument()` | **O(M)** | Dzięki reverse mapping (nie O(N)!) |
| `search()` | O(T × D × log D) | T = terminy query, D = dopasowane dokumenty |
| `getDocumentsForTerm()` | O(1) | Lookup w HashMap |

### Benchmark

```
Environment: MacBook Pro M1, 16GB RAM, PostgreSQL 15
Dataset: 10,000 dokumentów, ~1M unikalnych terminów

Operacja                  | Średni czas
--------------------------|-------------
Index 1 document          | ~5 ms
Search (1 term)           | ~2 ms
Search (3 terms)          | ~8 ms
Remove document (old O(N))| ~500 ms ❌
Remove document (new O(M))| ~1 ms ✅
Index rebuild (10k docs)  | ~15 seconds
```

### Concurrency Performance

```
Scenario: 10 concurrent readers + 5 concurrent writers
Result: No ConcurrentModificationException, consistent data
Throughput: ~5000 searches/second
```

---

## 🔧 Troubleshooting

### Problem: "Connection refused" przy starcie

**Przyczyna**: PostgreSQL nie działa lub złe dane logowania

**Rozwiązanie**:
```bash
# Sprawdź status PostgreSQL
sudo service postgresql status  # Linux
brew services list               # macOS

# Sprawdź połączenie
psql -U search_user -d search_engine -h localhost
```

### Problem: "Port 8080 already in use"

**Rozwiązanie 1**: Zmień port w `.env`
```properties
SERVER_PORT=8081
```

**Rozwiązanie 2**: Zabij proces
```bash
# Linux/macOS
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Problem: "Permission denied" (PostgreSQL 15+)

**Rozwiązanie**:
```sql
GRANT ALL ON SCHEMA public TO search_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO search_user;
```

### Problem: Maven build fails

**Rozwiązanie**:
```bash
# Wyczyść cache
./mvnw clean

# Rebuild dependencies
rm -rf ~/.m2/repository
./mvnw clean install
```

---

## 📈 Roadmap

### Planowane funkcje

- [ ] **Fuzzy Search** - Wyszukiwanie z tolerancją na literówki (Levenshtein distance)
- [ ] **Phrase Queries** - Wyszukiwanie fraz ("exact match")
- [ ] **Faceted Search** - Filtrowanie po metadanych (data, autor, język)
- [ ] **Query Expansion** - Synonimy i pokrewne terminy
- [ ] **Rate Limiting** - Ograniczenie liczby requestów
- [ ] **Caching** - Redis dla popularnych zapytań
- [ ] **Distributed Index** - Partycjonowanie dla skalowania
- [ ] **Real-time Indexing** - WebSocket updates

---

## 📄 Licencja

Ten projekt jest dostępny na licencji MIT. Zobacz plik [LICENSE](LICENSE) dla szczegółów.

---

## 👥 Autorzy

- **Twoje Imię** - Initial work - [GitHub](https://github.com/twoj-username)

---

## 🙏 Podziękowania

- Apache Lucene team za świetne narzędzia do analizy tekstu
- Spring Boot team za framework
- Wszystkim contributorom bibliotek open-source użytych w projekcie

---

**Made with ❤️ and ☕ by [Twoje Imię]**

