package com.bookrating.utils.exceptions;

public class GutendexServiceException extends RuntimeException {
    public GutendexServiceException(String message) {
        super(message);
    }

    public GutendexServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
