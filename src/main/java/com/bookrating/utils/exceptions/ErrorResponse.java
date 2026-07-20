package com.bookrating.utils.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int status, String message, List<String> errors) {
    public ErrorResponse(int status, String message) {
        this(status, message, null);
    }
}
