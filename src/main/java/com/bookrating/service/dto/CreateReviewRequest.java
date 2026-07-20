package com.bookrating.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @Min(value = 0, message = "Rating must be between 0 and 5")
        @Max(value = 5, message = "Rating must be between 0 and 5")
        int rating,

        @NotBlank(message = "Review must not be empty")
        @Size(max = 5000, message = "Review must not exceed 5000 characters")
        String review
) {}
