package com.bookrating.service.dto;

import com.bookrating.gateway.gutendex.GutendexAuthor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record BookDetailsDto(
        long id,
        String title,
        List<GutendexAuthor> authors,
        List<String> languages,
        @JsonProperty("download_count") int downloadCount,
        double rating,
        List<String> reviews
) {}
