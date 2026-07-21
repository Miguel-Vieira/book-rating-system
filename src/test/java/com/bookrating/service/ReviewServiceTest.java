package com.bookrating.service;

import com.bookrating.domain.ReviewEntity;
import com.bookrating.domain.repository.ReviewRepository;
import com.bookrating.gateway.gutendex.GutendexAuthor;
import com.bookrating.gateway.gutendex.GutendexBook;
import com.bookrating.service.dto.MonthlyRatingDto;
import com.bookrating.service.dto.CreateReviewRequest;
import com.bookrating.utils.exceptions.BookNotFoundException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@QuarkusTest
class ReviewServiceTest {

    @Inject
    ReviewService reviewService;

    @InjectMock
    ReviewRepository reviewRepository;

    @InjectMock
    BookSearchService bookSearchService;

    @Test
    void monthlyRatingsGroupsByYearMonth() {
        var jan1 = review(84L, 4, LocalDateTime.of(2025, 1, 10, 12, 0));
        var jan2 = review(84L, 2, LocalDateTime.of(2025, 1, 20, 15, 0));
        var feb1 = review(84L, 5, LocalDateTime.of(2025, 2, 5, 9, 0));

        when(reviewRepository.findByBookIdAll(84L)).thenReturn(List.of(jan1, jan2, feb1));

        List<MonthlyRatingDto> result = reviewService.getMonthlyRatings(84L);

        assertEquals(2, result.size());
        // sorted descending by month
        assertEquals("2025-02", result.get(0).month());
        assertEquals(5.0, result.get(0).averageRating());
        assertEquals(1, result.get(0).reviewCount());

        assertEquals("2025-01", result.get(1).month());
        assertEquals(3.0, result.get(1).averageRating()); // (4+2)/2
        assertEquals(2, result.get(1).reviewCount());
    }

    @Test
    void monthlyRatingsEmptyWhenNoReviews() {
        when(reviewRepository.findByBookIdAll(999L)).thenReturn(List.of());

        List<MonthlyRatingDto> result = reviewService.getMonthlyRatings(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createReviewValidatesBookExists() {
        when(bookSearchService.getBook(9999L)).thenThrow(new BookNotFoundException(9999L));

        assertThrows(BookNotFoundException.class,
                () -> reviewService.createReview(9999L, new CreateReviewRequest(4, "Great stuff")));

        verify(reviewRepository, never()).persist(Mockito.<ReviewEntity>any());
    }

    private ReviewEntity review(long bookId, int rating, LocalDateTime createdAt) {
        var entity = ReviewEntity.builder()
                .bookId(bookId)
                .rating(rating)
                .review("test review")
                .createdAt(createdAt)
                .build();
        return entity;
    }
}
