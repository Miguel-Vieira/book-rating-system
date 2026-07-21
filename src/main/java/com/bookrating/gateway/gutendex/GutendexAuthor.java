package com.bookrating.gateway.gutendex;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GutendexAuthor(
        String name,
        @JsonProperty("birth_year") Integer birthYear,
        @JsonProperty("death_year") Integer deathYear
) {}
