package com.adnane.moulcyber.application.admin;

import java.math.BigDecimal;

public record AdminGameResponse(
        Long id,
        String title,
        String description,
        BigDecimal rentalPrice) {
}
