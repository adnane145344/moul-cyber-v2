package com.adnane.moulcyber.application.rental;

import java.time.LocalDate;
import java.util.List;

import com.adnane.moulcyber.domain.rental.RentalStatus;

public record RentalResponse(
        Long id,
        RentalStatus status,
        LocalDate startDate,
        LocalDate dueDate,
        List<RentalItemResponse> items) {
}
