package com.adnane.moulcyber.application.review;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.adnane.moulcyber.application.catalog.GameNotFoundException;
import com.adnane.moulcyber.application.user.UserNotFoundException;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.rental.RentalItemStatus;
import com.adnane.moulcyber.domain.review.Review;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.rental.RentalItemRepository;
import com.adnane.moulcyber.infra.persistence.review.ReviewRepository;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final RentalItemRepository rentalItemRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewAssembler assembler;
    private final Clock clock;

    public ReviewService(
            GameRepository gameRepository,
            UserRepository userRepository,
            RentalItemRepository rentalItemRepository,
            ReviewRepository reviewRepository,
            ReviewAssembler assembler,
            Clock clock) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.rentalItemRepository = rentalItemRepository;
        this.reviewRepository = reviewRepository;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> findGameReviews(Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new GameNotFoundException();
        }
        return reviewRepository.findByGameIdOrderByCreatedAtDescIdDesc(gameId)
                .stream()
                .map(assembler::toResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse createReview(
            Long userId,
            Long gameId,
            CreateReviewRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(GameNotFoundException::new);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        boolean eligible = rentalItemRepository.existsCompletedRentalForGame(
                userId, gameId, RentalItemStatus.ACTIVE);
        if (!eligible) {
            throw new ReviewEligibilityException();
        }
        if (reviewRepository.existsByUserIdAndGameId(userId, gameId)) {
            throw new DuplicateReviewException();
        }

        Review review = new Review(
                null,
                user,
                game,
                request.rating(),
                request.comment(),
                Instant.now(clock));
        try {
            return assembler.toResponse(reviewRepository.saveAndFlush(review));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateReviewException();
        }
    }
}
