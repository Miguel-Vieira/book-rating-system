package com.bookrating.resources;

import com.bookrating.service.dto.BookDetailsDto;
import com.bookrating.service.dto.SearchResponseDto;
import com.bookrating.service.dto.TopBookDto;
import com.bookrating.service.BookDetailsService;
import com.bookrating.service.BookSearchService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class BookResource {

    @Inject
    BookSearchService bookSearchService;

    @Inject
    BookDetailsService bookDetailsService;

    @GET
    public SearchResponseDto searchBooks(
            @QueryParam("title") String title,
            @QueryParam("page") @DefaultValue("1") @Min(1) int page) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Query parameter 'title' is required");
        }
        return bookSearchService.searchBooks(title.trim(), page);
    }

    @GET
    @Path("/{bookId}")
    public BookDetailsDto getBookDetails(@PathParam("bookId") @Min(1) long bookId) {
        return bookDetailsService.getBookDetails(bookId);
    }

    @GET
    @Path("/top")
    public List<TopBookDto> getTopBooks(@QueryParam("limit") @DefaultValue("10") @Min(1) int limit) {
        return bookDetailsService.getTopBooks(Math.min(limit, 100));
    }
}
