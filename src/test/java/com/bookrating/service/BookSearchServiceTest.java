package com.bookrating.service;

import com.bookrating.gateway.gutendex.GutendexAuthor;
import com.bookrating.gateway.gutendex.GutendexBook;
import com.bookrating.gateway.gutendex.GutendexClient;
import com.bookrating.gateway.gutendex.GutendexSearchResponse;
import com.bookrating.service.dto.BookSearchResultDto;
import com.bookrating.service.dto.SearchResponseDto;
import com.bookrating.utils.exceptions.BookNotFoundException;
import com.bookrating.utils.exceptions.GutendexServiceException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ProcessingException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class BookSearchServiceTest {

    @Inject
    BookSearchService bookSearchService;

    @InjectMock
    @RestClient
    GutendexClient gutendexClient;

    @Test
    void searchMapsFieldsCorrectly() {
        var book = new GutendexBook(42, "Moby Dick",
                List.of(new GutendexAuthor("Melville, Herman", 1819, 1891)),
                List.of("en"), 75000);
        when(gutendexClient.searchBooks("moby", 1))
                .thenReturn(new GutendexSearchResponse(1, null, null, List.of(book)));

        SearchResponseDto result = bookSearchService.searchBooks("moby", 1);

        assertEquals(1, result.totalResults());
        BookSearchResultDto dto = result.books().get(0);
        assertEquals(42, dto.id());
        assertEquals("Moby Dick", dto.title());
        assertEquals("Melville, Herman", dto.authors().get(0).name());
        assertEquals(1819, dto.authors().get(0).birthYear());
        assertEquals(List.of("en"), dto.languages());
        assertEquals(75000, dto.downloadCount());
    }

    @Test
    void searchHandlesNullAuthorsAndLanguages() {
        var book = new GutendexBook(99, "Anonymous Work", null, null, 100);
        when(gutendexClient.searchBooks("anon", 1))
                .thenReturn(new GutendexSearchResponse(1, null, null, List.of(book)));

        SearchResponseDto result = bookSearchService.searchBooks("anon", 1);

        BookSearchResultDto dto = result.books().get(0);
        assertTrue(dto.authors().isEmpty());
        assertTrue(dto.languages().isEmpty());
    }

    @Test
    void getBookThrows404ForMissingBook() {
        when(gutendexClient.getBook(777L))
                .thenThrow(new WebApplicationException("Not Found", 404));

        assertThrows(BookNotFoundException.class,
                () -> bookSearchService.getBook(777L));
    }

    @Test
    void getBookWraps5xxAsServiceException() {
        when(gutendexClient.getBook(1L))
                .thenThrow(new WebApplicationException("Server Error", 500));

        assertThrows(GutendexServiceException.class,
                () -> bookSearchService.getBook(1L));
    }

    @Test
    void searchWrapsConnectionErrorAsServiceException() {
        when(gutendexClient.searchBooks("test", 1))
                .thenThrow(new ProcessingException("Connection refused"));

        assertThrows(GutendexServiceException.class,
                () -> bookSearchService.searchBooks("test", 1));
    }
}
