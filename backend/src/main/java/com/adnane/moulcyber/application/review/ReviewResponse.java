package com.adnane.moulcyber.application.review;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        int rating,
        String comment,
        String authorName,
        Instant createdAt) {
}
