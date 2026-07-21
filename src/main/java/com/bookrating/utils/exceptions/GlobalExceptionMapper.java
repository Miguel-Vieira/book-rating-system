package com.bookrating.utils.exceptions;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof BookRatingException bre) {
            if (bre.getStatusCode() >= 500) {
                log.error(bre.getMessage(), bre);
            }
            return error(bre.getStatusCode(), bre.getMessage());
        }

        if (exception instanceof ConstraintViolationException cve) {
            var messages = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .toList();
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorResponse(400, "Validation failed", messages))
                    .build();
        }

        if (exception instanceof WebApplicationException wae) {
            return error(wae.getResponse().getStatus(), wae.getMessage());
        }

        log.error("Unhandled exception", exception);
        return error(500, "Internal server error");
    }

    private Response error(int status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(status, message))
                .build();
    }
}
