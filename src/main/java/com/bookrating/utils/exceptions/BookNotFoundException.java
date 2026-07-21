package com.bookrating.utils.exceptions;

import jakarta.ws.rs.core.Response;

public class BookNotFoundException extends BookRatingException {
    public BookNotFoundException(long bookId) {
        super(Response.Status.NOT_FOUND, "Book not found with id: " + bookId);
    }
}
