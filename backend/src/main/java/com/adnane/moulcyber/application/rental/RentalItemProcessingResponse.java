package com.adnane.moulcyber.application.rental;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.domain.rental.RentalItemStatus;

public record RentalItemProcessingResponse(
        Long rentalItemId,
        RentalItemStatus status,
        GameCopyStatus copyStatus,
        LocalDate processedDate,
        BigDecimal lateFee) {
}
