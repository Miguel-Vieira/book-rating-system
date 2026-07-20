package com.bookrating.service.dto;

import java.time.LocalDateTime;

public record ReviewDto(
        Long id,
        long bookId,
        int rating,
        String review,
        LocalDateTime createdAt
) {}
