package com.bookrating.utils.exceptions;

public abstract class BookRatingException extends RuntimeException {

    private final int statusCode;

    protected BookRatingException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    protected BookRatingException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
