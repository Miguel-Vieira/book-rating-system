package com.bookrating.service.dto;

import java.util.List;

public record SearchResponseDto(
        int totalResults,
        int page,
        List<BookSearchResultDto> books
) {}
