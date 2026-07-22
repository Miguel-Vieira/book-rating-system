PRAGMA journal_mode=WAL;
PRAGMA synchronous=NORMAL;
CREATE INDEX IF NOT EXISTS idx_reviews_book_id ON reviews(book_id);
