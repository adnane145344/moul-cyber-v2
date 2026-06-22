package com.adnane.moulcyber.domain.rental;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.adnane.moulcyber.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalItem> items = new ArrayList<>();

    protected Rental() {
    }

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
        RentalItem validatedItem = Objects.requireNonNull(item, "Rental item is required.");
        validatedItem.attachTo(this);
        items.add(validatedItem);
    }

    public boolean isOverdueOn(LocalDate referenceDate) {
        LocalDate validatedReferenceDate = requireDate(referenceDate, "Reference date");
        return getStatus() == RentalStatus.ACTIVE && validatedReferenceDate.isAfter(dueDate);
    }

    public RentalStatus getStatus() {
        return items.stream().anyMatch(RentalItem::isActive)
                ? RentalStatus.ACTIVE
                : RentalStatus.COMPLETED;
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
