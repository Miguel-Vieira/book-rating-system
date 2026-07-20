package com.bookrating.domain.repository;

import com.bookrating.domain.ReviewEntity;
import com.bookrating.service.dto.MonthlyRatingDto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ReviewRepository implements PanacheRepository<ReviewEntity> {

    public List<ReviewEntity> findByBookId(long bookId) {
        return list("bookId", bookId);
    }

    public List<ReviewEntity> findByBookId(long bookId, int page, int size) {
        return find("bookId", bookId)
                .page(Page.of(page - 1, size))
                .list();
    }

    public double getAverageRating(long bookId) {
        Double avg = getEntityManager()
                .createQuery("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.bookId = :bookId", Double.class)
                .setParameter("bookId", bookId)
                .getSingleResult();
        return avg != null ? avg : 0.0;
    }

    public List<Object[]> getTopRatedBooks(int limit) {
        return getEntityManager()
                .createQuery(
                        "SELECT r.bookId, AVG(r.rating), COUNT(r) FROM ReviewEntity r " +
                                "GROUP BY r.bookId " +
                                "HAVING COUNT(r) >= 1 " +
                                "ORDER BY AVG(r.rating) DESC, COUNT(r) DESC",
                        Object[].class)
                .setMaxResults(limit)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<MonthlyRatingDto> getMonthlyRatings(long bookId) {
        // SQLite stores LocalDateTime as epoch numeric — group in Java via JPQL year/month extraction isn't supported,
        // so we pull minimal data and group. But since Hibernate maps to Java types, we can use FUNCTION for strftime.
        // Fallback: use Java grouping on just the fields we need.
        List<ReviewEntity> reviews = list("bookId", bookId);
        if (reviews.isEmpty()) {
            return List.of();
        }
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
}
