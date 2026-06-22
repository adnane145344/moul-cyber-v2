package com.adnane.moulcyber.api.review;

import java.util.List;

import com.adnane.moulcyber.application.review.CreateReviewRequest;
import com.adnane.moulcyber.application.review.ReviewResponse;
import com.adnane.moulcyber.application.review.ReviewService;
import com.adnane.moulcyber.configuration.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/{gameId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> findReviews(@PathVariable Long gameId) {
        return reviewService.findGameReviews(gameId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long gameId,
            @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createReview(principal.userId(), gameId, request);
    }
}
