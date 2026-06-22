package com.adnane.moulcyber.application.catalog;

import java.math.BigDecimal;

public record GameSummaryResponse(
        Long id,
        String title,
        BigDecimal rentalPrice,
        long availableCopies) {
}
