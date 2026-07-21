package com.bookrating.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ReviewDto(
        Long id,
        @JsonProperty("book_id") long bookId,
        int rating,
        String review,
        @JsonProperty("created_at") LocalDateTime createdAt
) {}
