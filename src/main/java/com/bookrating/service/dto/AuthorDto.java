package com.bookrating.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorDto(
        String name,
        @JsonProperty("birth_year") Integer birthYear,
        @JsonProperty("death_year") Integer deathYear
) {}
