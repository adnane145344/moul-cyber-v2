package com.adnane.moulcyber.application.admin;

import java.time.LocalDate;
import java.util.List;

import com.adnane.moulcyber.application.rental.RentalItemResponse;
import com.adnane.moulcyber.domain.rental.RentalStatus;

public record AdminRentalDetailsResponse(
        Long id,
        Long userId,
        String customerName,
        String customerEmail,
        RentalStatus status,
        boolean overdue,
        LocalDate startDate,
        LocalDate dueDate,
        List<RentalItemResponse> items) {
}
