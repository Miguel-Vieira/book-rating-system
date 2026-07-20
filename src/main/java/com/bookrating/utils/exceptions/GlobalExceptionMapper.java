package com.bookrating.utils.exceptions;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof BookNotFoundException) {
            return error(404, exception.getMessage());
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

        if (exception instanceof GutendexServiceException) {
            LOG.error("Gutendex API error", exception);
            return error(503, "Book service temporarily unavailable");
        }

        if (exception instanceof WebApplicationException wae) {
            return error(wae.getResponse().getStatus(), wae.getMessage());
        }

        LOG.error("Unhandled exception", exception);
        return error(500, "Internal server error");
    }

    private Response error(int status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(status, message))
                .build();
    }
}
