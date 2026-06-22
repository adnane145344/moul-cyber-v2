package com.adnane.moulcyber.domain.rental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import com.adnane.moulcyber.domain.inventory.GameCopy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rental_items")
public class RentalItem {

    private static final BigDecimal DAILY_LATE_FEE = new BigDecimal("2.00");
    private static final BigDecimal NO_LATE_FEE = new BigDecimal("0.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_copy_id", nullable = false)
    private GameCopy gameCopy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalItemStatus status;

    @Column(name = "processed_date")
    private LocalDate processedDate;

    @Column(name = "rental_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal rentalPrice;

    @Column(name = "late_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal lateFee;

    protected RentalItem() {
    }

    public RentalItem(GameCopy gameCopy, BigDecimal rentalPrice) {
        this(null, gameCopy, rentalPrice);
    }

    public RentalItem(Long id, GameCopy gameCopy, BigDecimal rentalPrice) {
        this.id = id;
        this.gameCopy = Objects.requireNonNull(gameCopy, "Game copy is required.");
        this.rentalPrice = requireNonNegative(rentalPrice, "Rental price");
        this.status = RentalItemStatus.ACTIVE;
        this.lateFee = NO_LATE_FEE;
    }

    public void returnOn(LocalDate returnDate) {
        requireActive();
        LocalDate validatedDate = requireProcessingDate(returnDate);
        long lateDays = ChronoUnit.DAYS.between(rental.getDueDate(), validatedDate);
        processedDate = validatedDate;
        if (lateDays > 0) {
            status = RentalItemStatus.LATE_RETURNED;
            lateFee = DAILY_LATE_FEE.multiply(BigDecimal.valueOf(lateDays));
        } else {
            status = RentalItemStatus.RETURNED;
            lateFee = NO_LATE_FEE;
        }
        gameCopy.markAsReturned();
    }

    public void markAsLost(LocalDate processingDate) {
        requireActive();
        processedDate = requireProcessingDate(processingDate);
        status = RentalItemStatus.LOST;
        lateFee = NO_LATE_FEE;
        gameCopy.markAsLost();
    }

    public void markAsDamaged(LocalDate processingDate) {
        requireActive();
        processedDate = requireProcessingDate(processingDate);
        status = RentalItemStatus.DAMAGED;
        lateFee = NO_LATE_FEE;
        gameCopy.markAsDamaged();
    }

    public Long getId() {
        return id;
    }

    public GameCopy getGameCopy() {
        return gameCopy;
    }

    public Rental getRental() {
        return rental;
    }

    public RentalItemStatus getStatus() {
        return status;
    }

    public LocalDate getProcessedDate() {
        return processedDate;
    }

    public BigDecimal getRentalPrice() {
        return rentalPrice;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public boolean isActive() {
        return status == RentalItemStatus.ACTIVE;
    }

    void attachTo(Rental rental) {
        this.rental = Objects.requireNonNull(rental, "Rental is required.");
    }

    private void requireActive() {
        if (!isActive()) {
            throw new InvalidRentalItemStatusException(
                    "Rental item has already been processed with status " + status + ".");
        }
    }

    private LocalDate requireProcessingDate(LocalDate processingDate) {
        LocalDate validatedDate = Objects.requireNonNull(
                processingDate, "Processing date is required.");
        if (rental == null) {
            throw new IllegalStateException("Rental item must belong to a rental.");
        }
        if (validatedDate.isBefore(rental.getStartDate())) {
            throw new InvalidRentalPeriodException(
                    "Processing date cannot be before rental start date.");
        }
        return validatedDate;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required.");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
        return value;
    }
}
