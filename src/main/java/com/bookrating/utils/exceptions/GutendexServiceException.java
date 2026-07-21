package com.bookrating.utils.exceptions;

import jakarta.ws.rs.core.Response;

public class GutendexServiceException extends BookRatingException {
    public GutendexServiceException(String message) {
        super(Response.Status.SERVICE_UNAVAILABLE, message);
    }

    public GutendexServiceException(String message, Throwable cause) {
        super(Response.Status.SERVICE_UNAVAILABLE, message, cause);
    }
}
