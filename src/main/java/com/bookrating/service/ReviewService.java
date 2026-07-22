package com.bookrating.service;

import com.bookrating.service.dto.CreateReviewRequest;
import com.bookrating.service.dto.MonthlyRatingDto;
import com.bookrating.service.dto.ReviewDto;
import com.bookrating.domain.ReviewEntity;
import com.bookrating.domain.repository.ReviewRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ReviewService {

    @Inject
    ReviewRepository reviewRepository;

    @Inject
    BookSearchService bookSearchService;

    public ReviewDto createReview(long bookId, CreateReviewRequest request) {
        bookSearchService.getBook(bookId);
        return persistReview(bookId, request);
    }

    @Transactional
    ReviewDto persistReview(long bookId, CreateReviewRequest request) {
        var entity = ReviewEntity.builder()
                .bookId(bookId)
                .rating(request.rating())
                .review(request.review())
                .build();
        reviewRepository.persist(entity);
        return toDto(entity);
    }

    @Transactional
    public List<ReviewDto> getReviewsForBook(long bookId, int page, int size) {
        return reviewRepository.findByBookId(bookId, page, size).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public List<ReviewDto> getReviewsForBook(long bookId) {
        return reviewRepository.findByBookId(bookId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public double getAverageRating(long bookId) {
        return reviewRepository.getAverageRating(bookId);
    }

    @Transactional
    public List<Object[]> getTopRatedBooks(int limit) {
        return reviewRepository.getTopRatedBooks(limit);
    }

    @Transactional
    public List<MonthlyRatingDto> getMonthlyRatings(long bookId) {
        bookSearchService.getBook(bookId);
        List<ReviewEntity> reviews = reviewRepository.findByBookId(bookId);
        if (reviews.isEmpty()) {
            return List.of();
        }
        // SQLite stores LocalDateTime as epoch, can't use SQL date functions
        return reviews.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getCreatedAt().getYear() + "-" + String.format("%02d", r.getCreatedAt().getMonthValue())))
                .entrySet().stream()
                .map(e -> new MonthlyRatingDto(
                        e.getKey(),
                        e.getValue().stream().mapToInt(ReviewEntity::getRating).average().orElse(0.0),
                        e.getValue().size()))
                .sorted((a, b) -> b.month().compareTo(a.month()))
                .toList();
    }

    private ReviewDto toDto(ReviewEntity entity) {
        return new ReviewDto(entity.id, entity.getBookId(), entity.getRating(), entity.getReview(), entity.getCreatedAt());
    }
}
