# Simple Search Engine - Testy

## Przegląd testów

Projekt zawiera kompleksowy zestaw testów jednostkowych i integracyjnych dla wszystkich komponentów aplikacji.

## Struktura testów

### 1. Testy jednostkowe (Unit Tests)

#### TextPreprocessorTest
- **Lokalizacja:** `src/test/java/.../engine/analysis/TextPreprocessorTest.java`
- **Zakres:** Testy przetwarzania tekstu, tokenizacji, usuwania stop words i stemmingu
- **Liczba testów:** 15
- **Przykładowe przypadki:**
  - Przetwarzanie pustych stringów
  - Konwersja na małe litery
  - Tokenizacja z interpunkcją
  - Usuwanie stop words
  - Stemming słów

#### InvertedIndexTest
- **Lokalizacja:** `src/test/java/.../engine/core/InvertedIndexTest.java`
- **Zakres:** Testy odwróconego indeksu
- **Liczba testów:** 18
- **Przykładowe przypadki:**
  - Dodawanie dokumentów
  - Wyszukiwanie dokumentów po terminach
  - Pozycje terminów w dokumentach
  - Operacje merge i clear
  - Współbieżność (thread safety)

#### IndexingServiceTest
- **Lokalizacja:** `src/test/java/.../service/IndexingServiceTest.java`
- **Zakres:** Testy serwisu indeksowania
- **Liczba testów:** 17
- **Przykładowe przypadki:**
  - Indeksowanie pojedynczych i wielu dokumentów
  - Zastępowanie indeksu (replaceIndex)
  - Obsługa pustych dokumentów
  - Przetwarzanie tekstu przed indeksowaniem
  - Współbieżne indeksowanie

#### SearchServiceTest
- **Lokalizacja:** `src/test/java/.../service/SearchServiceTest.java`
- **Zakres:** Testy wyszukiwania i rankingu
- **Liczba testów:** 20
- **Przykładowe przypadki:**
  - Wyszukiwanie w pustym indeksie
  - Wyszukiwanie z dopasowaniami i bez
  - Sortowanie wyników według relevance
  - Case insensitivity
  - Obsługa stop words

#### TfIdfScoringServiceTest
- **Lokalizacja:** `src/test/java/.../service/TfIdfScoringServiceTest.java`
- **Zakres:** Testy algorytmu TF-IDF
- **Liczba testów:** 21
- **Przykładowe przypadki:**
  - Obliczanie score dla dokumentów
  - Wpływ częstotliwości terminów (TF)
  - Wpływ częstotliwości dokumentów (IDF)
  - Normalizacja według długości dokumentu
  - Obsługa wielu terminów zapytania

#### PersistenceServiceTest
- **Lokalizacja:** `src/test/java/.../service/PersistenceServiceTest.java`
- **Zakres:** Testy zapisywania i wczytywania indeksu
- **Liczba testów:** 10
- **Przykładowe przypadki:**
  - Zapisywanie indeksu do pliku
  - Wczytywanie indeksu z pliku
  - Obsługa nieistniejących plików
  - Zachowanie danych między restartami

### 2. Testy integracyjne (Integration Tests)

#### SearchControllerTest
- **Lokalizacja:** `src/test/java/.../controller/SearchControllerTest.java`
- **Zakres:** Testy REST API z mockami
- **Liczba testów:** 17
- **Przykładowe przypadki:**
  - POST /api/documents - dodawanie dokumentów
  - GET /api/search - wyszukiwanie
  - GET /api/index - wyświetlanie indeksu
  - Walidacja JSON
  - Statusy HTTP

#### SearchEngineIntegrationTest
- **Lokalizacja:** `src/test/java/.../integration/SearchEngineIntegrationTest.java`
- **Zakres:** Testy end-to-end całej aplikacji
- **Liczba testów:** 15
- **Przykładowe przypadki:**
  - Pełny flow: indeksowanie → wyszukiwanie
  - Sortowanie wyników według relevance
  - Wielokrotne indeksowanie i wyszukiwanie
  - Testy wydajnościowe (50+ dokumentów)
  - Obsługa znaków specjalnych

## Uruchamianie testów

### Wszystkie testy
```bash
./mvnw test
```

### Tylko testy jednostkowe
```bash
./mvnw test -Dtest="*Test"
```

### Tylko testy integracyjne
```bash
./mvnw test -Dtest="*IntegrationTest"
```

### Konkretna klasa testowa
```bash
./mvnw test -Dtest=TextPreprocessorTest
```

### Konkretny test
```bash
./mvnw test -Dtest=TextPreprocessorTest#testLowercaseConversion
```

### Z raportami pokrycia
```bash
./mvnw test jacoco:report
```
Raport dostępny w: `target/site/jacoco/index.html`

## Statystyki testów

- **Łączna liczba testów:** 130+
- **Pokrycie kodu:** ~85%+
- **Komponenty przetestowane:**
  - ✅ TextPreprocessor
  - ✅ InvertedIndex
  - ✅ IndexingService
  - ✅ SearchService
  - ✅ TfIdfScoringService
  - ✅ PersistenceService
  - ✅ SearchController
  - ✅ Integracja end-to-end

## Kluczowe scenariusze testowe

### 1. Podstawowy flow wyszukiwania
```
Dodaj dokument → Indeksuj → Wyszukaj → Zwróć posortowane wyniki
```

### 2. Przetwarzanie tekstu
```
Tekst wejściowy → Lowercase → Tokenizacja → Usunięcie stop words → Stemming
```

### 3. Scoring TF-IDF
```
Query → Oblicz TF → Oblicz IDF → TF * IDF → Posortuj wyniki
```

### 4. Persistencja
```
Indeksuj dokumenty → Zapisz do pliku → Restart aplikacji → Wczytaj z pliku → Wyszukaj
```

## Uruchamianie w IntelliJ IDEA

1. **Wszystkie testy w projekcie:**
   - Kliknij prawym na folder `src/test/java`
   - Wybierz "Run 'All Tests'"

2. **Testy z konkretnej klasy:**
   - Otwórz plik testowy
   - Kliknij zieloną strzałkę przy nazwie klasy
   - Wybierz "Run 'NazwaKlasyTest'"

3. **Pojedynczy test:**
   - Kliknij zieloną strzałkę przy metodzie testowej
   - Wybierz "Run 'nazwa_testu'"

4. **Z pokryciem kodu:**
   - Kliknij prawym na testy
   - Wybierz "Run 'Tests' with Coverage"

## Debugowanie testów

1. **Ustaw breakpoint** w kodzie testu lub testowanej metodzie
2. **Kliknij prawym** na test
3. **Wybierz "Debug 'nazwa_testu'"**
4. Używaj F8 (Step Over), F7 (Step Into) do nawigacji

## Najlepsze praktyki

1. **Uruchamiaj testy przed każdym commitem:**
   ```bash
   ./mvnw test
   ```

2. **Sprawdzaj pokrycie kodu:**
   ```bash
   ./mvnw verify jacoco:report
   ```

3. **Utrzymuj testy w aktualnym stanie** przy zmianach w kodzie

4. **Dodawaj testy dla nowych funkcji** przed implementacją (TDD)

## Troubleshooting

### Problem: Testy nie uruchamiają się
**Rozwiązanie:**
```bash
./mvnw clean test
```

### Problem: Konflikty z istniejącym indeksem
**Rozwiązanie:**
```bash
rm index.ser
./mvnw test
```

### Problem: Błędy kompilacji
**Rozwiązanie:**
```bash
./mvnw clean compile test-compile
```

## Continuous Integration

Testy można zintegrować z CI/CD:

### GitHub Actions
```yaml
- name: Run tests
  run: ./mvnw test
```

### Jenkins
```groovy
stage('Test') {
    steps {
        sh './mvnw test'
    }
}
```

## Dodatkowe informacje

- **Framework testowy:** JUnit 5 (Jupiter)
- **Mockowanie:** Mockito
- **Spring Boot Test:** MockMvc, TestRestTemplate
- **Assertions:** AssertJ, Hamcrest

