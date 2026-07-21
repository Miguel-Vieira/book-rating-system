package com.bookrating.service;

import com.bookrating.gateway.gutendex.GutendexBook;
import com.bookrating.service.dto.AuthorDto;
import com.bookrating.service.dto.BookDetailsDto;
import com.bookrating.service.dto.ReviewDto;
import com.bookrating.service.dto.TopBookDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JBossLog
@ApplicationScoped
public class BookDetailsService {

    @Inject
    BookSearchService bookSearchService;

    @Inject
    ReviewService reviewService;

    public BookDetailsDto getBookDetails(long bookId) {
        GutendexBook book = bookSearchService.getBook(bookId);
        List<ReviewDto> reviews = reviewService.getReviewsForBook(bookId);
        double avg = reviewService.getAverageRating(bookId);

        return new BookDetailsDto(
                book.id(),
                book.title(),
                mapAuthors(book),
                Objects.requireNonNullElse(book.languages(), Collections.emptyList()),
                book.downloadCount(),
                Math.round(avg * 100.0) / 100.0,
                reviews.stream().map(ReviewDto::review).toList()
        );
    }

    public List<TopBookDto> getTopBooks(int limit) {
        return reviewService.getTopRatedBooks(limit).stream()
                .map(row -> {
                    long bookId = ((Number) row[0]).longValue();
                    double avg = ((Number) row[1]).doubleValue();
                    long count = ((Number) row[2]).longValue();
                    return new TopBookDto(bookId, Math.round(avg * 100.0) / 100.0, count, fetchTitle(bookId));
                })
                .toList();
    }

    private String fetchTitle(long bookId) {
        try {
            return bookSearchService.getBook(bookId).title();
        } catch (Exception e) {
            log.warnf("Could not fetch title for book %d: %s", bookId, e.getMessage());
            return null;
        }
    }

    private List<AuthorDto> mapAuthors(GutendexBook book) {
        if (book.authors() == null) return Collections.emptyList();
        return book.authors().stream()
                .map(a -> new AuthorDto(a.name(), a.birthYear(), a.deathYear()))
                .toList();
    }
}
