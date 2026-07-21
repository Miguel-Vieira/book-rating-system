package com.bookrating.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SearchResponseDto(
        @JsonProperty("total_results") int totalResults,
        int page,
        List<BookSearchResultDto> books
) {}
