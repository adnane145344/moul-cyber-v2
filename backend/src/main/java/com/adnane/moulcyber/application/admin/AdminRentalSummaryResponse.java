package com.adnane.moulcyber.application.admin;

import java.time.LocalDate;

import com.adnane.moulcyber.domain.rental.RentalStatus;

public record AdminRentalSummaryResponse(
        Long id,
        Long userId,
        String customerName,
        String customerEmail,
        RentalStatus status,
        boolean overdue,
        LocalDate startDate,
        LocalDate dueDate,
        int itemCount) {
}
