package com.adnane.moulcyber.catalog;

import java.math.BigDecimal;
import java.util.Objects;

public class Game {

    private final Long id;
    private final String title;
    private final String description;
    private final BigDecimal rentalPrice;

    public Game(String title, String description, BigDecimal rentalPrice) {
        this(null, title, description, rentalPrice);
    }

    public Game(Long id, String title, String description, BigDecimal rentalPrice) {
        this.id = id;
        this.title = requireText(title, "Title");
        this.description = requireText(description, "Description");
        this.rentalPrice = requireNonNegative(rentalPrice);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getRentalPrice() {
        return rentalPrice;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    private static BigDecimal requireNonNegative(BigDecimal value) {
        Objects.requireNonNull(value, "Rental price is required.");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Rental price cannot be negative.");
        }
        return value;
    }
}
