package com.bookrating.utils.exceptions;

public class GutendexServiceException extends BookRatingException {
    public GutendexServiceException(String message) {
        super(503, message);
    }

    public GutendexServiceException(String message, Throwable cause) {
        super(503, message, cause);
    }
}
