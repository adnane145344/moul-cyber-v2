package com.adnane.moulcyber.application.catalog;

import java.math.BigDecimal;

public record GameDetailsResponse(
        Long id,
        String title,
        String description,
        BigDecimal rentalPrice,
        long availableCopies) {
}
