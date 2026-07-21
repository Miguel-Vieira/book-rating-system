package com.bookrating.utils.exceptions;

public class BookNotFoundException extends BookRatingException {
    public BookNotFoundException(long bookId) {
        super(404, "Book not found with id: " + bookId);
    }
}
