package com.bookrating.utils.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<BookRatingException> {

    @Override
    public Response toResponse(BookRatingException exception) {
        if (exception.getStatus().getFamily() == Response.Status.Family.SERVER_ERROR) {
            log.error(exception.getMessage(), exception);
        }
        return Response.status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(exception.getStatus().getStatusCode(), exception.getMessage()))
                .build();
    }
}
