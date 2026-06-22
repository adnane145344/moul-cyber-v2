package com.adnane.moulcyber.application.review;

import com.adnane.moulcyber.domain.review.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewAssembler {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getFirstName(),
                review.getCreatedAt());
    }
}
