package com.bookrating.gateway.gutendex;

import java.util.List;

public record GutendexSearchResponse(
        int count,
        String next,
        String previous,
        List<GutendexBook> results
) {}
