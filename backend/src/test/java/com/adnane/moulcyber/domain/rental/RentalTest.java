package com.adnane.moulcyber.domain.rental;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RentalTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 6, 1);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 6, 8);
    private static final BigDecimal RENTAL_PRICE = new BigDecimal("5.00");

    private final User customer = new User(
            "Adnane", "Lardi", "adnane@example.com", "password-hash", Role.CLIENT);

    @Test
    void rentalPeriodMustBeValid() {
        assertThatThrownBy(() -> new Rental(customer, null, DUE_DATE))
                .isInstanceOf(InvalidRentalPeriodException.class);
        assertThatThrownBy(() -> new Rental(customer, START_DATE, null))
                .isInstanceOf(InvalidRentalPeriodException.class);
        assertThatThrownBy(() -> new Rental(customer, START_DATE, START_DATE))
                .isInstanceOf(InvalidRentalPeriodException.class);
    }

    @Test
    void newRentalItemIsActiveAndKeepsRentalPriceSnapshot() {
        RentalItem item = item();

        assertThat(item.getStatus()).isEqualTo(RentalItemStatus.ACTIVE);
        assertThat(item.getRentalPrice()).isEqualByComparingTo("5.00");
        assertThat(item.getLateFee()).isEqualByComparingTo("0.00");
        assertThat(item.getProcessedDate()).isNull();
    }

    @Test
    void rentalIsActiveWhileAnItemIsActiveAndCompletedAfterProcessing() {
        Rental rental = rentalWithItem();
        RentalItem item = rental.getItems().getFirst();

        assertThat(rental.getStatus()).isEqualTo(RentalStatus.ACTIVE);
        item.getGameCopy().rent();
        item.returnOn(DUE_DATE);

        assertThat(rental.getStatus()).isEqualTo(RentalStatus.COMPLETED);
    }

    @Test
    void normalReturnMakesCopyAvailableWithoutLateFee() {
        Rental rental = rentalWithItem();
        RentalItem item = rental.getItems().getFirst();
        item.getGameCopy().rent();

        item.returnOn(DUE_DATE);

        assertThat(item.getStatus()).isEqualTo(RentalItemStatus.RETURNED);
        assertThat(item.getProcessedDate()).isEqualTo(DUE_DATE);
        assertThat(item.getLateFee()).isEqualByComparingTo("0.00");
        assertThat(item.getGameCopy().getStatus()).isEqualTo(GameCopyStatus.AVAILABLE);
    }

    @Test
    void lateReturnCalculatesTwoDollarsPerLateDay() {
        Rental rental = rentalWithItem();
        RentalItem item = rental.getItems().getFirst();
        item.getGameCopy().rent();

        item.returnOn(DUE_DATE.plusDays(3));

        assertThat(item.getStatus()).isEqualTo(RentalItemStatus.LATE_RETURNED);
        assertThat(item.getLateFee()).isEqualByComparingTo("6.00");
    }

    @Test
    void lostAndDamagedItemsKeepCopiesUnavailable() {
        Rental lostRental = rentalWithItem();
        RentalItem lostItem = lostRental.getItems().getFirst();
        lostItem.getGameCopy().rent();
        lostItem.markAsLost(DUE_DATE);

        Rental damagedRental = rentalWithItem();
        RentalItem damagedItem = damagedRental.getItems().getFirst();
        damagedItem.getGameCopy().rent();
        damagedItem.markAsDamaged(DUE_DATE);

        assertThat(lostItem.getStatus()).isEqualTo(RentalItemStatus.LOST);
        assertThat(lostItem.getGameCopy().getStatus()).isEqualTo(GameCopyStatus.LOST);
        assertThat(damagedItem.getStatus()).isEqualTo(RentalItemStatus.DAMAGED);
        assertThat(damagedItem.getGameCopy().getStatus()).isEqualTo(GameCopyStatus.DAMAGED);
    }

    @Test
    void processedItemCannotBeProcessedTwice() {
        Rental rental = rentalWithItem();
        RentalItem item = rental.getItems().getFirst();
        item.getGameCopy().rent();
        item.returnOn(DUE_DATE);

        assertThatThrownBy(() -> item.markAsLost(DUE_DATE.plusDays(1)))
                .isInstanceOf(InvalidRentalItemStatusException.class);
    }

    @Test
    void activeRentalBecomesOverdueAfterDueDate() {
        Rental rental = rentalWithItem();

        assertThat(rental.isOverdueOn(DUE_DATE)).isFalse();
        assertThat(rental.isOverdueOn(DUE_DATE.plusDays(1))).isTrue();
    }

    @Test
    void itemsCollectionIsImmutable() {
        Rental rental = rentalWithItem();

        assertThatThrownBy(() -> rental.getItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private Rental rentalWithItem() {
        Rental rental = new Rental(customer, START_DATE, DUE_DATE);
        rental.addItem(item());
        return rental;
    }

    private RentalItem item() {
        Game game = new Game(
                "Cyber Quest", "A cooperative science-fiction adventure.", RENTAL_PRICE);
        return new RentalItem(new GameCopy(game), RENTAL_PRICE);
    }
}
