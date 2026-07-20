package com.bookrating.service;

import com.bookrating.gateway.gutendex.GutendexBook;
import com.bookrating.service.dto.AuthorDto;
import com.bookrating.service.dto.BookDetailsDto;
import com.bookrating.service.dto.ReviewDto;
import com.bookrating.service.dto.TopBookDto;
import com.bookrating.domain.repository.ReviewRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class BookDetailsService {

    private static final Logger LOG = Logger.getLogger(BookDetailsService.class);

    @Inject
    BookSearchService bookSearchService;

    @Inject
    ReviewService reviewService;

    @Inject
    ReviewRepository reviewRepository;

    public BookDetailsDto getBookDetails(long bookId) {
        GutendexBook book = bookSearchService.getBook(bookId);
        List<ReviewDto> reviews = reviewService.getReviewsForBook(bookId);

        double avg = reviews.isEmpty() ? 0.0 :
                reviews.stream().mapToInt(ReviewDto::rating).average().orElse(0.0);

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
        List<Object[]> rows = queryTopRated(limit);
        // map titles outside the transaction to avoid holding DB connection during HTTP calls
        return rows.stream()
                .map(row -> {
                    long bookId = ((Number) row[0]).longValue();
                    double avg = ((Number) row[1]).doubleValue();
                    long count = ((Number) row[2]).longValue();
                    return new TopBookDto(bookId, Math.round(avg * 100.0) / 100.0, count, fetchTitle(bookId));
                })
                .toList();
    }

    @Transactional
    List<Object[]> queryTopRated(int limit) {
        return reviewRepository.getTopRatedBooks(limit);
    }

    private String fetchTitle(long bookId) {
        try {
            return bookSearchService.getBook(bookId).title();
        } catch (Exception e) {
            LOG.warnf("Could not fetch title for book %d: %s", bookId, e.getMessage());
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
