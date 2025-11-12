# 🔍 Simple Search Engine

A full-featured search engine built with Java and Spring Boot, featuring web crawling, text indexing, and TF-IDF ranking algorithm.

## ✨ Features

- **Web Crawler** - Automatic website crawling with depth control
- **Inverted Index** - Fast document retrieval and search
- **TF-IDF Ranking** - Intelligent document scoring and ranking
- **PostgreSQL Database** - Persistent storage for documents and crawl history
- **REST API** - Complete API for search, crawling, and document management
- **Swagger UI** - Interactive API documentation

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **PostgreSQL** - Database
- **Jsoup** - Web scraping
- **Snowball Stemmer** - Text processing
- **JUnit 5 & Mockito** - Testing

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.6+
- PostgreSQL 15+

### Setup

1. **Create PostgreSQL database:**
```bash
psql postgres
CREATE DATABASE search_engine;
CREATE USER search_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE search_engine TO search_user;
\q
```

2. **Set environment variables:**
```bash
export DB_URL=jdbc:postgresql://localhost:5432/search_engine
export DB_USERNAME=search_user
export DB_PASSWORD=your_password
```

3. **Run the application:**
```bash
./mvnw spring-boot:run
```

4. **Open Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

## 📖 API Usage

### Crawl a website
```http
POST http://localhost:8080/api/crawler/start
Content-Type: application/json

{
  "startUrl": "https://example.com",
  "maxPages": 10,
  "maxDepth": 2,
  "delayMs": 1000
}
```

### Search documents
```http
GET http://localhost:8080/api/search?query=java+spring&limit=10&offset=0
```

### Add document manually
```http
POST http://localhost:8080/api/documents
Content-Type: application/json

{
  "title": "Java Tutorial",
  "content": "Learn Java programming with examples...",
  "url": "https://example.com/java"
}
```

### Get crawl history
```http
GET http://localhost:8080/api/crawler/history
```

## 🏗️ Architecture

```
Controller Layer    → REST endpoints
    ↓
Service Layer       → Business logic (crawling, indexing, searching)
    ↓
Repository Layer    → Database access (Spring Data JPA)
    ↓
Database           → PostgreSQL
```

### Key Components

- **InvertedIndex** - Core data structure for fast text search
- **TextPreprocessor** - Tokenization, stopword removal, stemming
- **TfIdfScoringService** - Document ranking algorithm
- **CrawlerService** - Web scraping with BFS traversal

## 📊 Database Schema

### documents
```sql
id, title, content, url, created_at, updated_at, crawled_at
```

### crawl_history
```sql
id, start_url, started_at, finished_at, status, 
pages_crawled, documents_indexed, duration_ms, error_message
```

## 🧪 Testing

Run all tests:
```bash
./mvnw test
```

The project includes 85+ tests covering:
- Unit tests (services, algorithms)
- Integration tests (REST API)
- All tests passing ✅

## 📁 Project Structure

```
src/main/java/.../simplesearchengine/
├── controller/        # REST endpoints
├── service/           # Business logic
├── model/             # JPA entities
├── repository/        # Database access
├── engine/
│   ├── core/         # Inverted Index
│   └── analysis/     # Text preprocessing
├── dto/              # Data Transfer Objects
├── exception/        # Custom exceptions
└── config/           # Configuration
```

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 📝 How It Works

1. **Crawling** - Jsoup fetches web pages, extracts content
2. **Preprocessing** - Text is tokenized, normalized, stemmed
3. **Indexing** - Terms are stored in inverted index structure
4. **Searching** - Query is processed and matched against index
5. **Ranking** - TF-IDF algorithm scores and ranks results

### TF-IDF Formula
```
score = TF(term, doc) × IDF(term, corpus)

TF  = term frequency in document
IDF = log(total_docs / docs_containing_term)
```

## 🎯 Example Workflow

```bash
# 1. Crawl a website
curl -X POST http://localhost:8080/api/crawler/start \
  -H "Content-Type: application/json" \
  -d '{"startUrl":"https://example.com","maxPages":5,"maxDepth":2,"delayMs":1000}'

# 2. Search for documents
curl "http://localhost:8080/api/search?query=example&limit=10"

# 3. View crawl history
curl http://localhost:8080/api/crawler/history
```

## 🐛 Troubleshooting

**Database connection error:**
- Check PostgreSQL is running: `brew services list`
- Verify environment variables are set

**Port 8080 already in use:**
- Change port in `application.properties`: `server.port=8081`

## 📄 License

This project is open source and available under the MIT License.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## 👨‍💻 Author

Created as a learning project for building search engines from scratch.

---

**Made with ☕ and Spring Boot**

