# 🚀 REST API - Simple Search Engine

## 📋 Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/documents` | Add new document |
| GET | `/api/documents` | Get all documents |
| GET | `/api/documents/{id}` | Get document by ID |
| GET | `/api/documents/url?url=` | Get document by URL |
| PUT | `/api/documents/{id}` | Update document |
| DELETE | `/api/documents/{id}` | Delete document |
| GET | `/api/documents/count` | Count documents |
| DELETE | `/api/documents` | Delete all documents |
| GET | `/api/search?q=` | Search documents (existing) |

---

## 🧪 Examples

### 1. Add Document

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Machine Learning Basics",
    "content": "Machine learning is a subset of artificial intelligence...",
    "url": "https://example.com/ml-basics"
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "Machine Learning Basics",
  "content": "Machine learning is a subset of artificial intelligence...",
  "url": "https://example.com/ml-basics",
  "createdAt": "2025-11-08T22:00:00",
  "updatedAt": null
}
```

---

### 2. Get All Documents

```bash
curl http://localhost:8080/api/documents
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Machine Learning Basics",
    "content": "...",
    "url": "https://example.com/ml-basics",
    "createdAt": "2025-11-08T22:00:00",
    "updatedAt": null
  }
]
```

---

### 3. Get Document by ID

```bash
curl http://localhost:8080/api/documents/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Machine Learning Basics",
  "content": "...",
  "url": "https://example.com/ml-basics",
  "createdAt": "2025-11-08T22:00:00",
  "updatedAt": null
}
```

**Error (404 Not Found):**
```json
{
  "timestamp": "2025-11-08T22:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Document not found: ID=999"
}
```

---

### 4. Get Document by URL

```bash
curl "http://localhost:8080/api/documents/url?url=https://example.com/ml-basics"
```

---

### 5. Update Document

```bash
curl -X PUT http://localhost:8080/api/documents/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Machine Learning UPDATED",
    "content": "Updated content about machine learning",
    "url": "https://example.com/ml-basics"
  }'
```

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Machine Learning UPDATED",
  "content": "Updated content about machine learning",
  "url": "https://example.com/ml-basics",
  "createdAt": "2025-11-08T22:00:00",
  "updatedAt": "2025-11-08T22:15:00"
}
```

---

### 6. Delete Document

```bash
curl -X DELETE http://localhost:8080/api/documents/1
```

**Response (204 No Content):**
```
(empty body)
```

---

### 7. Count Documents

```bash
curl http://localhost:8080/api/documents/count
```

**Response (200 OK):**
```
5
```

---

### 8. Search Documents

```bash
curl "http://localhost:8080/api/search?q=machine+learning"
```

**Response (200 OK):**
```json
{
  "results": [
    {
      "documentId": 1,
      "content": "Machine learning is a subset of artificial intelligence...",
      "score": 0.95
    }
  ],
  "totalResults": 1,
  "page": 0,
  "pageSize": 1,
  "totalPages": 1
}
```

---

## 🎨 HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful GET/PUT |
| 201 | Created | Document added successfully |
| 204 | No Content | Delete successful |
| 404 | Not Found | Document doesn't exist |
| 409 | Conflict | Duplicate URL |
| 500 | Internal Server Error | Unexpected error |

---

## 📚 Swagger UI

**Access API documentation at:**
```
http://localhost:8080/swagger-ui.html
```

**OpenAPI JSON:**
```
http://localhost:8080/v3/api-docs
```

---

## 🧪 Testing with Postman

### Import Collection:

1. Open Postman
2. Import → Link → Paste:
   ```
   http://localhost:8080/v3/api-docs
   ```
3. Generate Collection
4. Test all endpoints!

---

## ⚙️ Configuration

Add to `application.properties`:

```properties
# Swagger
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

---
