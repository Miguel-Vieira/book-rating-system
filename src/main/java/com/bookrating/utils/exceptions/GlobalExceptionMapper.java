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
            if (bre.getStatus().getFamily() == Response.Status.Family.SERVER_ERROR) {
                log.error(bre.getMessage(), bre);
            }
            return error(bre.getStatus(), bre.getMessage());
        }

        if (exception instanceof ConstraintViolationException cve) {
            var messages = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .toList();
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "Validation failed", messages))
                    .build();
        }

        if (exception instanceof WebApplicationException wae) {
            return error(Response.Status.fromStatusCode(wae.getResponse().getStatus()), wae.getMessage());
        }

        log.error("Unhandled exception", exception);
        return error(Response.Status.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private Response error(Response.Status status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(status.getStatusCode(), message))
                .build();
    }
}
