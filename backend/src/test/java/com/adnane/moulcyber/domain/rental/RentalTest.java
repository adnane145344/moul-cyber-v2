package com.adnane.moulcyber.domain.rental;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RentalTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 6, 1);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 6, 8);

    private final User customer = new User(
            "Adnane",
            "Lardi",
            "adnane@example.com",
            "password-hash",
            Role.CUSTOMER);

    @Test
    void rentalStartDateIsRequired() {
        assertThatThrownBy(() -> new Rental(customer, null, DUE_DATE))
                .isInstanceOf(InvalidRentalPeriodException.class)
                .hasMessageContaining("Start date");
    }

    @Test
    void rentalDueDateIsRequired() {
        assertThatThrownBy(() -> new Rental(customer, START_DATE, null))
                .isInstanceOf(InvalidRentalPeriodException.class)
                .hasMessageContaining("Due date");
    }

    @Test
    void rentalDueDateMustBeAfterStartDate() {
        assertThatThrownBy(() -> new Rental(customer, START_DATE, START_DATE))
                .isInstanceOf(InvalidRentalPeriodException.class);

        assertThatThrownBy(() -> new Rental(customer, START_DATE, START_DATE.minusDays(1)))
                .isInstanceOf(InvalidRentalPeriodException.class);
    }

    @Test
    void activeRentalIsNotOverdueOnOrBeforeDueDate() {
        Rental rental = new Rental(customer, START_DATE, DUE_DATE);

        assertThat(rental.isOverdueOn(DUE_DATE.minusDays(1))).isFalse();
        assertThat(rental.isOverdueOn(DUE_DATE)).isFalse();
    }

    @Test
    void activeRentalIsOverdueAfterDueDate() {
        Rental rental = new Rental(customer, START_DATE, DUE_DATE);

        assertThat(rental.isOverdueOn(DUE_DATE.plusDays(1))).isTrue();
    }

    @Test
    void returnedRentalIsNoLongerActiveOrOverdue() {
        Rental rental = new Rental(customer, START_DATE, DUE_DATE);
        rental.returnOn(DUE_DATE.plusDays(2));

        assertThat(rental.isOverdueOn(DUE_DATE.plusDays(3))).isFalse();
        assertThat(rental.getReturnedDate()).isEqualTo(DUE_DATE.plusDays(2));
    }

    @Test
    void returnDateCannotBeBeforeStartDate() {
        Rental rental = new Rental(customer, START_DATE, DUE_DATE);

        assertThatThrownBy(() -> rental.returnOn(START_DATE.minusDays(1)))
                .isInstanceOf(InvalidRentalPeriodException.class);
    }

    @Test
    void rentalReturnedOnTimeHasNoLateFee() {
        Rental rental = new Rental(customer, START_DATE, DUE_DATE);
        rental.returnOn(DUE_DATE);

        assertThat(rental.wasReturnedLate()).isFalse();
        assertThat(rental.calculateLateFee()).isEqualByComparingTo("0.00");
    }

    @Test
    void lateRentalCalculatesTwoDollarsPerLateDay() {
        Rental rental = new Rental(customer, START_DATE, DUE_DATE);
        rental.returnOn(DUE_DATE.plusDays(3));

        assertThat(rental.wasReturnedLate()).isTrue();
        assertThat(rental.calculateLateFee()).isEqualByComparingTo("6.00");
    }

    @Test
    void activeRentalHasNoFinalLateFee() {
        Rental rental = new Rental(customer, START_DATE, DUE_DATE);

        assertThat(rental.calculateLateFee()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void rentalCanContainItemsWithoutExposingMutableCollection() {
        Rental rental = new Rental(customer, START_DATE, DUE_DATE);
        RentalItem item = new RentalItem(new GameCopy(new Game(
                "Cyber Quest",
                "A cooperative science-fiction adventure.",
                new BigDecimal("5.00"))));

        rental.addItem(item);

        assertThat(rental.getItems()).containsExactly(item);
        assertThatThrownBy(() -> rental.getItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
