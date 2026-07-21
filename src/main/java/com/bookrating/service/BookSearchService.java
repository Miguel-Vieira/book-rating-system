package com.bookrating.service;

import com.bookrating.gateway.gutendex.GutendexBook;
import com.bookrating.gateway.gutendex.GutendexClient;
import com.bookrating.gateway.gutendex.GutendexSearchResponse;
import com.bookrating.service.dto.AuthorDto;
import com.bookrating.service.dto.BookSearchResultDto;
import com.bookrating.service.dto.SearchResponseDto;
import com.bookrating.utils.exceptions.BookNotFoundException;
import com.bookrating.utils.exceptions.GutendexServiceException;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@JBossLog
@ApplicationScoped
public class BookSearchService {

    @Inject
    @RestClient
    GutendexClient gutendexClient;

    // avoid hammering Gutendex on repeated lookups
    @CacheResult(cacheName = "gutendex-search")
    public SearchResponseDto searchBooks(String title, int page) {
        try {
            GutendexSearchResponse response = gutendexClient.searchBooks(title.trim(), page);
            List<BookSearchResultDto> books = response.results().stream()
                    .map(this::toSearchResult)
                    .toList();
            return new SearchResponseDto(response.count(), page, books);
        } catch (WebApplicationException e) {
            log.errorf("Gutendex returned %d for title='%s'", e.getResponse().getStatus(), title);
            throw new GutendexServiceException("Book search unavailable", e);
        } catch (ProcessingException e) {
            log.errorf(e, "Connection error searching Gutendex for '%s'", title);
            throw new GutendexServiceException("Book search unavailable", e);
        }
    }

    @CacheResult(cacheName = "gutendex-book")
    public GutendexBook getBook(long bookId) {
        try {
            return gutendexClient.getBook(bookId);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                throw new BookNotFoundException(bookId);
            }
            log.errorf("Gutendex returned %d for book %d", e.getResponse().getStatus(), bookId);
            throw new GutendexServiceException("Book service unavailable", e);
        } catch (ProcessingException e) {
            log.errorf(e, "Connection error fetching book %d", bookId);
            throw new GutendexServiceException("Book service unavailable", e);
        }
    }

    private BookSearchResultDto toSearchResult(GutendexBook book) {
        var authors = book.authors() != null
                ? book.authors().stream().map(a -> new AuthorDto(a.name(), a.birthYear(), a.deathYear())).toList()
                : List.<AuthorDto>of();
        return new BookSearchResultDto(
                book.id(),
                book.title(),
                authors,
                book.languages() != null ? book.languages() : List.of(),
                book.downloadCount());
    }
}
