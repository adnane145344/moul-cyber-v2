package com.adnane.moulcyber.application.rental;

import java.time.LocalDate;

import com.adnane.moulcyber.domain.rental.RentalStatus;

public record RentalSummaryResponse(
        Long id,
        RentalStatus status,
        LocalDate startDate,
        LocalDate dueDate,
        int itemCount) {
}
