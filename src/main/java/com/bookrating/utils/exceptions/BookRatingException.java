package com.bookrating.utils.exceptions;

import jakarta.ws.rs.core.Response;

public abstract class BookRatingException extends RuntimeException {

    private final Response.Status status;

    protected BookRatingException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

    protected BookRatingException(Response.Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}
