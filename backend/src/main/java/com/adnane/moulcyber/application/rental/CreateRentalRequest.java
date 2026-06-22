package com.adnane.moulcyber.application.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRentalRequest(
        @NotNull(message = "Game id is required.")
        @Positive(message = "Game id must be positive.")
        Long gameId) {
}
