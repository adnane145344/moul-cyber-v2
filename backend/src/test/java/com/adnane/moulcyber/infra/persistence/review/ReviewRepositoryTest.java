package com.adnane.moulcyber.infra.persistence.review;

import java.math.BigDecimal;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.review.Review;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void canFindReviewsForGameAndDetectExistingReview() {
        User customer = userRepository.saveAndFlush(customer());
        Game game = gameRepository.saveAndFlush(game());
        reviewRepository.saveAndFlush(new Review(customer, game, 5, "Excellent game."));

        assertThat(reviewRepository.existsByUserIdAndGameId(customer.getId(), game.getId())).isTrue();
        assertThat(reviewRepository.findByGameId(game.getId()))
                .singleElement()
                .satisfies(review -> {
                    assertThat(review.getRating()).isEqualTo(5);
                    assertThat(review.getComment()).isEqualTo("Excellent game.");
                });
    }

    @Test
    void userCannotReviewSameGameTwice() {
        User customer = userRepository.saveAndFlush(customer());
        Game game = gameRepository.saveAndFlush(game());
        reviewRepository.saveAndFlush(new Review(customer, game, 4, "Great game."));

        assertThatThrownBy(() ->
                reviewRepository.saveAndFlush(new Review(customer, game, 5, "Still excellent.")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User customer() {
        return new User(
                "Adnane",
                "Lardi",
                "review.customer@example.com",
                "password-hash",
                Role.CUSTOMER);
    }

    private Game game() {
        return new Game(
                "Cyber Quest",
                "A cooperative science-fiction adventure.",
                new BigDecimal("5.00"));
    }
}
