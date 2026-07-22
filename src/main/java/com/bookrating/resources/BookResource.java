package com.bookrating.resources;

import com.bookrating.service.dto.BookDetailsDto;
import com.bookrating.service.dto.SearchResponseDto;
import com.bookrating.service.dto.TopBookDto;
import com.bookrating.service.BookDetailsService;
import com.bookrating.service.BookSearchService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {

    @Inject
    BookSearchService bookSearchService;

    @Inject
    BookDetailsService bookDetailsService;

    @GET
    public SearchResponseDto searchBooks(
            @QueryParam("title") @NotBlank(message = "Query parameter 'title' is required") String title,
            @QueryParam("page") @DefaultValue("1") @Positive int page) {
        return bookSearchService.searchBooks(title.trim(), page);
    }

    @GET
    @Path("/{bookId}")
    public BookDetailsDto getBookDetails(@PathParam("bookId") @Positive long bookId) {
        return bookDetailsService.getBookDetails(bookId);
    }

    @GET
    @Path("/top")
    public List<TopBookDto> getTopBooks(@QueryParam("limit") @DefaultValue("10") @Positive int limit) {
        return bookDetailsService.getTopBooks(Math.min(limit, 20));
    }
}
