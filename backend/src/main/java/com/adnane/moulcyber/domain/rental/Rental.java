package com.adnane.moulcyber.domain.rental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.adnane.moulcyber.domain.user.User;

public class Rental {

    private static final BigDecimal DAILY_LATE_FEE = new BigDecimal("2.00");
    private static final BigDecimal NO_LATE_FEE = new BigDecimal("0.00");

    private final Long id;
    private final User user;
    private final LocalDate startDate;
    private final LocalDate dueDate;
    private final List<RentalItem> items = new ArrayList<>();
    private LocalDate returnedDate;

    public Rental(User user, LocalDate startDate, LocalDate dueDate) {
        this(null, user, startDate, dueDate);
    }

    public Rental(Long id, User user, LocalDate startDate, LocalDate dueDate) {
        this.id = id;
        this.user = Objects.requireNonNull(user, "User is required.");
        this.startDate = requireDate(startDate, "Start date");
        this.dueDate = requireDate(dueDate, "Due date");

        if (!this.dueDate.isAfter(this.startDate)) {
            throw new InvalidRentalPeriodException("Due date must be after start date.");
        }
    }

    public void addItem(RentalItem item) {
        items.add(Objects.requireNonNull(item, "Rental item is required."));
    }

    public void returnOn(LocalDate returnDate) {
        LocalDate validatedReturnDate = requireDate(returnDate, "Return date");
        if (validatedReturnDate.isBefore(startDate)) {
            throw new InvalidRentalPeriodException("Return date cannot be before start date.");
        }
        if (returnedDate != null) {
            throw new InvalidRentalPeriodException("Rental has already been returned.");
        }
        returnedDate = validatedReturnDate;
    }

    public boolean isOverdueOn(LocalDate referenceDate) {
        LocalDate validatedReferenceDate = requireDate(referenceDate, "Reference date");
        return returnedDate == null && validatedReferenceDate.isAfter(dueDate);
    }

    public boolean wasReturnedLate() {
        return returnedDate != null && returnedDate.isAfter(dueDate);
    }

    public BigDecimal calculateLateFee() {
        if (!wasReturnedLate()) {
            return NO_LATE_FEE;
        }

        long lateDays = ChronoUnit.DAYS.between(dueDate, returnedDate);
        return DAILY_LATE_FEE.multiply(BigDecimal.valueOf(lateDays));
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public List<RentalItem> getItems() {
        return List.copyOf(items);
    }

    private static LocalDate requireDate(LocalDate value, String fieldName) {
        if (value == null) {
            throw new InvalidRentalPeriodException(fieldName + " is required.");
        }
        return value;
    }
}
