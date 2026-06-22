package com.adnane.moulcyber.application.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @Min(value = 1, message = "Rating must be at least 1.")
        @Max(value = 5, message = "Rating must not exceed 5.")
        int rating,

        @NotBlank(message = "Comment is required.")
        @Size(max = 1000, message = "Comment must not exceed 1000 characters.")
        String comment) {
}
