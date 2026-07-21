package com.bookrating.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MonthlyRatingDto(
        String month,
        @JsonProperty("average_rating") double averageRating,
        @JsonProperty("review_count") long reviewCount
) {}
