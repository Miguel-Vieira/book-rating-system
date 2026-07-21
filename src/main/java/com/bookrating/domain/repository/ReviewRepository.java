package com.bookrating.domain.repository;

import com.bookrating.domain.ReviewEntity;
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

    public List<ReviewEntity> findByBookIdAll(long bookId) {
        return list("bookId", bookId);
    }
}
