# Quarkus Performance Patterns - Learnings

**Last Updated:** 2026-07-20  
**Source:** Book Rating System performance review

---

## Key Anti-Patterns Observed

### 1. SQLite Connection Pool Misconfiguration

**Pattern:** `quarkus.datasource.jdbc.max-size=1` with concurrent web traffic

**Why it's wrong:**
- SQLite supports multiple concurrent readers with WAL mode
- Single connection creates artificial serialization point
- All requests queue even for read-only operations

**Correct pattern:**
```properties
quarkus.datasource.jdbc.max-size=10
quarkus.datasource.jdbc.additional-jdbc-properties.journal_mode=WAL
quarkus.datasource.jdbc.additional-jdbc-properties.synchronous=NORMAL
```

**Impact:** 5-10x throughput improvement under concurrent load

---

### 2. Blocking I/O Inside Transaction Boundaries

**Pattern:** External HTTP calls or long computations inside `@Transactional` methods

**Example:**
```java
@Transactional
public ReviewDto createReview(long bookId, CreateReviewRequest request) {
    bookSearchService.getBook(bookId);  // HTTP call inside transaction!
    reviewRepository.persist(entity);
}
```

**Why it's wrong:**
- Database connection held during network I/O (100ms+)
- Reduces effective connection pool size
- SQLite write locks held unnecessarily
- Timeout scenarios (10s) block connection for entire duration

**Correct pattern:**
```java
public ReviewDto createReview(long bookId, CreateReviewRequest request) {
    // Validation BEFORE transaction
    bookSearchService.getBook(bookId);
    return createReviewEntity(bookId, request);
}

@Transactional
private ReviewDto createReviewEntity(long bookId, CreateReviewRequest request) {
    // Only DB work inside transaction
    reviewRepository.persist(entity);
}
```

**Impact:** Transaction duration 100ms → <1ms (100x improvement)

---

### 3. In-Memory Aggregation vs SQL Aggregation

**Pattern:** Loading full result sets to compute aggregates in Java

**Example:**
```java
List<ReviewEntity> reviews = reviewRepository.findByBookId(bookId);
double avg = reviews.stream()
    .mapToInt(ReviewEntity::getRating)
    .average()
    .orElse(0.0);
```

**Why it's wrong:**
- Loads N entities (e.g., 10,000) to compute 1 number
- Excessive memory allocation and GC pressure
- Network/serialization overhead if results cross process boundaries
- Database already optimized for aggregations

**Correct pattern:**
```java
// Repository
@Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.bookId = :bookId")
Double getAverageRating(@Param("bookId") long bookId);
```

**Impact:** 99% memory reduction, 50-90% latency reduction

**Rule of thumb:** If you're using Java Streams to `group()`, `count()`, or `average()`, you probably should be using SQL.

---

### 4. N+1 HTTP Calls in Batch Operations

**Pattern:** Sequential external API calls in loop/stream operations

**Example:**
```java
return topBooks.stream()
    .map(book -> {
        String title = gutendexClient.getBook(book.getId()).title();
        return new TopBookDto(book.getId(), title);
    })
    .toList();
```

**Why it's wrong:**
- N sequential network calls (10 × 100ms = 1000ms minimum)
- Cannot leverage HTTP/2 multiplexing
- Degrades linearly with N
- Cache helps but doesn't fix the pattern

**Solutions by priority:**

**1. Denormalize (best):**
```java
// Store frequently accessed fields locally
@Column(name = "book_title")
private String bookTitle;  // Cached from external API
```

**2. Use reactive parallel fetch:**
```java
Multi.createFrom().iterable(bookIds)
    .onItem().transformToUniAndConcatenate(id -> 
        gutendexClient.getBook(id))
    .collect().asList();
```

**3. Batch API if available:**
```java
gutendexClient.getBooks(bookIds);  // Single call with multiple IDs
```

**Impact:** 10-100x latency improvement

---

### 5. Unbounded Result Sets Without Pagination

**Pattern:** Repository methods returning `List<Entity>` without LIMIT clause

**Example:**
```java
public List<ReviewEntity> findByBookId(long bookId) {
    return list("bookId", bookId);  // No LIMIT!
}
```

**Why it's wrong:**
- Popular entities accumulate unbounded children (10,000+ reviews)
- Single request can OOM the application
- API response sizes grow unbounded (5-10 MB+)
- Frontend rendering performance degrades

**Correct pattern:**
```java
public List<ReviewEntity> findByBookId(long bookId, int page, int limit) {
    return find("bookId = ?1 ORDER BY createdAt DESC", bookId)
        .page(page - 1, limit)
        .list();
}
```

**Always add:**
- `@QueryParam("limit") @DefaultValue("20") @Max(100) int limit`
- Return total count for pagination UX
- Document maximum values in OpenAPI spec

**Impact:** Prevents OOM, 80-90% latency reduction for large datasets

---

### 6. Overly Aggressive Cache TTLs

**Pattern:** Short TTLs (5-15 minutes) for effectively immutable data

**Context:** Caching external APIs with stable content (e.g., book catalogs, public datasets)

**Why it's wrong:**
- Public domain book metadata changes rarely (if ever)
- 15-minute TTL means 96 cache misses per day per book
- Wastes network bandwidth and upstream API quota
- Higher latency for users (cache miss = 100ms+ HTTP call)

**Correct approach:**
```properties
# Immutable content: 24 hours or longer
quarkus.cache.caffeine."book-metadata".expire-after-write=24H
quarkus.cache.caffeine."book-metadata".maximum-size=10000

# Frequently changing: shorter TTL
quarkus.cache.caffeine."trending-books".expire-after-write=5M
quarkus.cache.caffeine."trending-books".maximum-size=100
```

**Decision framework:**
- Immutable content (historical data, public domain): 24H - 7D
- Rarely changing (catalogs, metadata): 1H - 24H
- Frequently updated (user-generated, trending): 1M - 15M
- Real-time (stock prices, live scores): No cache or 1-5s

**Always add:**
- `maximum-size` to prevent unbounded growth
- Metrics to monitor hit rate: `quarkus.cache.*.metrics-enabled=true`

**Impact:** 10-20x cache hit rate improvement, 96% reduction in upstream calls

---

### 7. Missing Composite Indexes for GROUP BY Queries

**Pattern:** Index on WHERE clause column, but not on GROUP BY/ORDER BY columns

**Example:**
```sql
-- Query
SELECT strftime('%Y-%m', created_at), AVG(rating)
FROM reviews
WHERE book_id = ?
GROUP BY strftime('%Y-%m', created_at)
ORDER BY created_at DESC;

-- Index (incomplete)
CREATE INDEX idx_reviews_book_id ON reviews(book_id);
```

**Why it's wrong:**
- Index covers WHERE but database still scans for GROUP BY/ORDER BY
- Opportunity for index-only scan is missed

**Correct pattern:**
```java
@Index(name = "idx_reviews_book_created", 
       columnList = "book_id, created_at")
```

**Index design rules:**
1. Columns in WHERE → first in index
2. Columns in GROUP BY/ORDER BY → next in index
3. Columns in SELECT → consider covering index
4. High cardinality → early in composite index

**Impact:** 30-50% query speedup, better scaling with data growth

---

## Quarkus-Specific Optimizations

### Cache Configuration Best Practices

```properties
# Always set maximum size to prevent memory leaks
quarkus.cache.caffeine."my-cache".maximum-size=10000

# Enable metrics for monitoring
quarkus.cache.caffeine."my-cache".metrics-enabled=true

# Use appropriate TTL based on data mutability
quarkus.cache.caffeine."my-cache".expire-after-write=1H
```

**Cache key generation:**
- Default: Method parameters automatically used as key
- Complex keys: Use `@CacheKey` on specific parameters
- Custom: Implement `CacheKeyGenerator`

---

### SQLite + Quarkus Configuration

```properties
# Connection pool
quarkus.datasource.jdbc.max-size=10

# Enable WAL mode for concurrent reads
quarkus.datasource.jdbc.additional-jdbc-properties.journal_mode=WAL
quarkus.datasource.jdbc.additional-jdbc-properties.synchronous=NORMAL

# For read-heavy workloads
quarkus.datasource.jdbc.additional-jdbc-properties.cache_size=10000
quarkus.datasource.jdbc.additional-jdbc-properties.temp_store=MEMORY
```

**When SQLite is NOT appropriate:**
- High write concurrency (>100 writes/sec)
- Multi-process scenarios (use PostgreSQL)
- Large datasets (>100 GB)
- Complex full-text search

**When SQLite IS appropriate:**
- Read-heavy workloads with moderate writes
- Single-process applications
- Embedded/edge deployments
- Development/testing environments

---

### Panache Query Optimization

**Avoid:**
```java
// Loads all entities into memory
List<Entity> all = Entity.listAll();
return all.stream()
    .filter(e -> e.isActive())
    .map(this::toDto)
    .toList();
```

**Prefer:**
```java
// Filter in database
return Entity.list("active = true").stream()
    .map(this::toDto)
    .toList();

// Better: Use projections
return Entity.find("active = true")
    .project(Dto.class)
    .list();
```

**Pagination pattern:**
```java
PanacheQuery<Entity> query = Entity.find("status = ?1", status);
return query.page(pageIndex, pageSize).list();
```

---

## Performance Testing Checklist

Before production deployment, verify:

- [ ] All list endpoints have pagination (`limit` + `offset` or `page`)
- [ ] No queries without WHERE clause in production code paths
- [ ] All GROUP BY queries have appropriate composite indexes
- [ ] External HTTP calls outside transaction boundaries
- [ ] Cache sizes bounded with `maximum-size`
- [ ] SQLite WAL mode enabled if using SQLite in production
- [ ] Load test with realistic concurrency (50+ users)
- [ ] Memory profiling under sustained load (no leaks)
- [ ] Query plan analysis (`EXPLAIN`) for slow endpoints
- [ ] Cache hit rate monitoring configured

---

## Red Flags in Code Review

Watch for these patterns:

```java
// 🚩 Stream aggregation on large datasets
list.stream().mapToInt(...).average()

// 🚩 Unbounded list() calls
repository.listAll()
repository.findByX(id)  // No limit parameter

// 🚩 HTTP inside @Transactional
@Transactional
void method() {
    externalApi.call();
}

// 🚩 Loop with external calls
for (Book book : books) {
    api.getDetails(book.getId());
}

// 🚩 Missing cache bounds
@CacheResult(cacheName = "my-cache")  // No max size configured

// 🚩 Loading entities to count
repository.findAll().size()  // Use COUNT query instead
```

---

## Metrics to Monitor

**Application metrics:**
```properties
quarkus.datasource.jdbc.enable-metrics=true
quarkus.cache.caffeine.*.metrics-enabled=true
quarkus.hibernate-orm.metrics.enabled=true
```

**Key indicators:**
- `hikaricp_connections_active` - Connection pool saturation
- `cache_gets_total` / `cache_gets_hit_total` - Cache hit rate
- `http_server_requests_seconds` - Endpoint latency (p95, p99)
- `jvm_memory_used_bytes` - Heap usage over time

**Alerting thresholds:**
- Connection pool >80% utilized
- Cache hit rate <70%
- p95 latency >500ms
- Heap usage >85%

---

## Resources

- Quarkus Performance Tuning: https://quarkus.io/guides/performance-measure
- Caffeine Cache Best Practices: https://github.com/ben-manes/caffeine/wiki/Efficiency
- SQLite WAL Mode: https://www.sqlite.org/wal.html
- Hibernate Query Performance: https://vladmihalcea.com/

---

**Pattern Summary:**
- Push computation to the database (SQL > Java Streams)
- Bound all result sets (pagination, LIMIT)
- Keep transactions small (no I/O inside)
- Cache smartly (TTL based on mutability, always set max size)
- Index for your queries (WHERE + GROUP BY + ORDER BY)
- Test under realistic load (50+ concurrent users)
