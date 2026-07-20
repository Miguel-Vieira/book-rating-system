package com.bookrating.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TopBookDto(
        @JsonProperty("book_id") long bookId,
        @JsonProperty("average_rating") double averageRating,
        @JsonProperty("review_count") long reviewCount,
        String title
) {}
