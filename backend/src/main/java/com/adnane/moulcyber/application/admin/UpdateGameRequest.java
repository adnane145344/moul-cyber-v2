package com.adnane.moulcyber.application.admin;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateGameRequest(
        @NotBlank(message = "Title is required.")
        @Size(max = 255, message = "Title must not exceed 255 characters.")
        String title,

        @NotBlank(message = "Description is required.")
        String description,

        @NotNull(message = "Rental price is required.")
        @DecimalMin(value = "0.01", message = "Rental price must be positive.")
        @Digits(integer = 8, fraction = 2, message = "Rental price must contain at most 8 integer and 2 decimal digits.")
        BigDecimal rentalPrice) {
}
