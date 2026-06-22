package com.adnane.moulcyber.application.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddGameCopiesRequest(
        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        @Max(value = 100, message = "Quantity must not exceed 100.")
        Integer quantity) {
}
