package com.adnane.moulcyber.domain.catalog;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "rental_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal rentalPrice;

    protected Game() {
    }

    public Game(String title, String description, BigDecimal rentalPrice) {
        this(null, title, description, rentalPrice);
    }

    public Game(Long id, String title, String description, BigDecimal rentalPrice) {
        this.id = id;
        this.title = requireTitle(title);
        this.description = requireText(description, "Description");
        this.rentalPrice = requirePositive(rentalPrice);
    }

    public void updateDetails(String title, String description, BigDecimal rentalPrice) {
        this.title = requireTitle(title);
        this.description = requireText(description, "Description");
        this.rentalPrice = requirePositive(rentalPrice);
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

    private static String requireTitle(String value) {
        String title = requireText(value, "Title");
        if (title.length() > 255) {
            throw new IllegalArgumentException("Title must not exceed 255 characters.");
        }
        return title;
    }

    private static BigDecimal requirePositive(BigDecimal value) {
        Objects.requireNonNull(value, "Rental price is required.");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("Rental price must be positive.");
        }
        return value;
    }
}
