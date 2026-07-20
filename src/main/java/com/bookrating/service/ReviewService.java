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

    @Transactional
    public ReviewDto createReview(long bookId, CreateReviewRequest request) {
        bookSearchService.getBook(bookId);

        var entity = ReviewEntity.builder()
                .bookId(bookId)
                .rating(request.rating())
                .review(request.review())
                .build();
        reviewRepository.persist(entity);
        return toDto(entity);
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
    public List<MonthlyRatingDto> getMonthlyRatings(long bookId) {
        List<ReviewEntity> reviews = reviewRepository.findByBookId(bookId);
        if (reviews.isEmpty()) {
            return List.of();
        }
        return reviews.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getCreatedAt().getYear() + "-" + String.format("%02d", r.getCreatedAt().getMonthValue())))
                .entrySet().stream()
                .map(entry -> new MonthlyRatingDto(
                        entry.getKey(),
                        entry.getValue().stream().mapToInt(ReviewEntity::getRating).average().orElse(0.0),
                        entry.getValue().size()))
                .sorted((a, b) -> b.month().compareTo(a.month()))
                .toList();
    }

    private ReviewDto toDto(ReviewEntity entity) {
        return new ReviewDto(entity.id, entity.getBookId(), entity.getRating(), entity.getReview(), entity.getCreatedAt());
    }
}

