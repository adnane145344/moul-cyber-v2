package com.adnane.moulcyber.application.rental;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.adnane.moulcyber.domain.rental.RentalItemStatus;

public record RentalItemResponse(
        Long id,
        Long gameId,
        String gameTitle,
        Long copyId,
        RentalItemStatus status,
        BigDecimal rentalPrice,
        LocalDate processedDate,
        BigDecimal lateFee) {
}
