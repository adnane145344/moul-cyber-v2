package com.adnane.moulcyber.domain.review;

import java.math.BigDecimal;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewTest {

    private final User customer = new User(
            "Adnane",
            "Lardi",
            "adnane@example.com",
            "password-hash",
            Role.CUSTOMER);

    private final Game game = new Game(
            "Cyber Quest",
            "A cooperative science-fiction adventure.",
            new BigDecimal("5.00"));

    @Test
    void reviewCanUseRatingBoundaries() {
        Review lowestRating = new Review(customer, game, 1, "Not for everyone.");
        Review highestRating = new Review(customer, game, 5, "Excellent game.");

        assertThat(lowestRating.getRating()).isEqualTo(1);
        assertThat(highestRating.getRating()).isEqualTo(5);
    }

    @Test
    void ratingMustBeBetweenOneAndFive() {
        assertThatThrownBy(() -> new Review(customer, game, 0, "Too low."))
                .isInstanceOf(InvalidReviewException.class);

        assertThatThrownBy(() -> new Review(customer, game, 6, "Too high."))
                .isInstanceOf(InvalidReviewException.class);
    }

    @Test
    void reviewCommentCannotBeNull() {
        assertThatThrownBy(() -> new Review(customer, game, 4, null))
                .isInstanceOf(InvalidReviewException.class);
    }

    @Test
    void reviewCommentCannotBeBlank() {
        assertThatThrownBy(() -> new Review(customer, game, 4, "   "))
                .isInstanceOf(InvalidReviewException.class);
    }

    @Test
    void reviewCommentIsTrimmed() {
        Review review = new Review(customer, game, 4, "  Great cooperative game.  ");

        assertThat(review.getComment()).isEqualTo("Great cooperative game.");
    }

    @Test
    void reviewRequiresAUser() {
        assertThatThrownBy(() -> new Review(null, game, 4, "Great game."))
                .isInstanceOf(InvalidReviewException.class);
    }

    @Test
    void reviewRequiresAGame() {
        assertThatThrownBy(() -> new Review(customer, null, 4, "Great game."))
                .isInstanceOf(InvalidReviewException.class);
    }
}
