package com.adnane.moulcyber.application.review;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.adnane.moulcyber.application.catalog.GameNotFoundException;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.rental.RentalItemStatus;
import com.adnane.moulcyber.domain.review.Review;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.rental.RentalItemRepository;
import com.adnane.moulcyber.infra.persistence.review.ReviewRepository;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-22T15:30:00Z");

    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RentalItemRepository rentalItemRepository;
    @Mock
    private ReviewRepository reviewRepository;

    private ReviewService reviewService;
    private User user;
    private Game game;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(
                gameRepository,
                userRepository,
                rentalItemRepository,
                reviewRepository,
                new ReviewAssembler(),
                Clock.fixed(NOW, ZoneOffset.UTC));
        user = new User(1L, "Adnane", "Lardi", "client@example.com",
                "password-hash", Role.CLIENT);
        game = new Game(2L, "Cyber Quest", "Adventure.", new BigDecimal("5.00"));
    }

    @Test
    void eligibleUserCanCreateReview() {
        when(gameRepository.findById(2L)).thenReturn(Optional.of(game));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(rentalItemRepository.existsCompletedRentalForGame(
                1L, 2L, RentalItemStatus.ACTIVE)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndGameId(1L, 2L)).thenReturn(false);
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            return new Review(10L, user, game, review.getRating(),
                    review.getComment(), review.getCreatedAt());
        });

        ReviewResponse response = reviewService.createReview(
                1L, 2L, new CreateReviewRequest(5, " Excellent game. "));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.comment()).isEqualTo("Excellent game.");
        assertThat(response.authorName()).isEqualTo("Adnane");
        assertThat(response.createdAt()).isEqualTo(NOW);
    }

    @Test
    void userWithoutCompletedRentalCannotReview() {
        when(gameRepository.findById(2L)).thenReturn(Optional.of(game));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(rentalItemRepository.existsCompletedRentalForGame(
                1L, 2L, RentalItemStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(
                1L, 2L, new CreateReviewRequest(5, "Excellent.")))
                .isInstanceOf(ReviewEligibilityException.class);
    }

    @Test
    void duplicateReviewFails() {
        when(gameRepository.findById(2L)).thenReturn(Optional.of(game));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(rentalItemRepository.existsCompletedRentalForGame(
                1L, 2L, RentalItemStatus.ACTIVE)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndGameId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(
                1L, 2L, new CreateReviewRequest(5, "Excellent.")))
                .isInstanceOf(DuplicateReviewException.class);
    }

    @Test
    void missingGameFailsAndReviewsAreListed() {
        when(gameRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reviewService.createReview(
                1L, 404L, new CreateReviewRequest(5, "Excellent.")))
                .isInstanceOf(GameNotFoundException.class);

        Review review = new Review(10L, user, game, 5, "Excellent.", NOW);
        when(gameRepository.existsById(2L)).thenReturn(true);
        when(reviewRepository.findByGameIdOrderByCreatedAtDescIdDesc(2L))
                .thenReturn(List.of(review));
        assertThat(reviewService.findGameReviews(2L))
                .singleElement()
                .satisfies(response -> assertThat(response.id()).isEqualTo(10L));
    }
}
