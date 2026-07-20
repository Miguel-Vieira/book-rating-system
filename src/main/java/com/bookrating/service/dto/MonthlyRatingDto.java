package com.bookrating.service.dto;

public record MonthlyRatingDto(
        String month,
        double averageRating,
        long reviewCount
) {}
