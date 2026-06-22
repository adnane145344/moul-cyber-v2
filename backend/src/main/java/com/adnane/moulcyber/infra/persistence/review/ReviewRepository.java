package com.adnane.moulcyber.infra.persistence.review;

import java.util.List;

import com.adnane.moulcyber.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndGameId(Long userId, Long gameId);

    List<Review> findByGameIdOrderByCreatedAtDescIdDesc(Long gameId);
}
