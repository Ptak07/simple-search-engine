# Simple Search Engine

Multi-language search engine with web crawling, inverted index, and TF-IDF ranking.

## Features

- **Multi-language support** - Polish (Stempel) & English (Snowball) text processing
- **Web crawler** - Sync/async crawling with BFS, throttling, cancellation
- **Full-text search** - Inverted index with TF-IDF ranking
- **REST API** - Complete CRUD for documents, search, crawler control
- **PostgreSQL** - Persistent storage with JPA/Hibernate
- **Thread pool** - Async processing (2-5 workers) with graceful degradation

## Tech Stack

**Backend:** Java 21, Spring Boot 3.5.7  
**Database:** PostgreSQL 15+  
**Text Processing:** Apache Lucene 9.11 (Stempel, Snowball)  
**Testing:** JUnit 5, Mockito (113 tests)

## Quick Start

```bash
# 1. Setup database
psql postgres
CREATE DATABASE search_engine;
CREATE USER search_user WITH PASSWORD 'search_password';
GRANT ALL PRIVILEGES ON DATABASE search_engine TO search_user;
\q

# 2. Configure
cp .env.example .env
# Edit .env with your database credentials

# 3. Run
./mvnw spring-boot:run

# 4. Test
open http://localhost:8080/swagger-ui/index.html
```

## API Examples

**Search** (Polish default, English optional)
```bash
GET /api/search?query=programowanie&language=pl
GET /api/search?query=programming&language=en&limit=10&offset=0
```

**Documents**
```bash
GET    /api/documents
POST   /api/documents {"title":"...", "content":"...", "language":"pl"}
PUT    /api/documents/1 {...}
DELETE /api/documents/1
```

**Crawler**
```bash
POST /api/crawler/start {"startUrl":"https://example.com", "maxPages":10, "language":"en"}
POST /api/crawler/start-async {...}
POST /api/crawler/cancel/1
GET  /api/crawler/history?status=SUCCESS
```

**System**
```bash
GET /swagger-ui/index.html # Interactive API docs
```

## Architecture

```
Controller → Service → Repository → PostgreSQL
                 ↓
            InvertedIndex (in-memory search)
```

**Core Components:**  
`InvertedIndex` - term -> document mapping  
`TextProcessor` - tokenization, stemming (Stempel/Snowball)  
`TfIdfScoring` - document ranking  
`CrawlerService` - BFS web scraping with async execution

**Database:**  
`document` - id, title, content, url, language, timestamps  
`crawl_history` - id, start_url, status, pages_crawled, documents_indexed

## Testing

```bash
./mvnw test  # 113 tests 
```

## Project Structure

```
src/main/java/.../simplesearchengine/
├── controller/         # REST API
├── service/            # Business logic
├── repository/         # JPA repositories
├── model/              # Entities
├── engine/
│   ├── core/          # InvertedIndex, TF-IDF
│   └── analysis/      # Text processors (Polish/English)
├── dto/               # Request/Response objects
└── config/            # Spring configuration
```

## How It Works

1. **Crawl** - Jsoup extracts HTML content  
2. **Process** - Tokenize → Remove stop words → Stem (Stempel/Snowball)  
3. **Index** - Store in inverted index (term → document IDs)  
4. **Search** - Process query → Match terms → Rank by TF-IDF  
5. **Return** - Top results with snippets

**TF-IDF:** `score = TF(term, doc) × log(total_docs / docs_with_term)`

---


