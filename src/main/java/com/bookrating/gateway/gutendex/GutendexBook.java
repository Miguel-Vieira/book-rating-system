package com.bookrating.gateway.gutendex;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GutendexBook(
        long id,
        String title,
        List<GutendexAuthor> authors,
        List<String> languages,
        @JsonProperty("download_count") int downloadCount
) {}
