package com.adnane.moulcyber.inventory;

import java.math.BigDecimal;

import com.adnane.moulcyber.catalog.Game;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameCopyTest {

    private final Game game = new Game(
            "Cyber Quest",
            "A cooperative science-fiction adventure.",
            new BigDecimal("5.00"));

    @Test
    void newCopyIsAvailable() {
        GameCopy copy = new GameCopy(game);

        assertThat(copy.getStatus()).isEqualTo(GameCopyStatus.AVAILABLE);
    }

    @Test
    void availableCopyCanBeMarkedAsRented() {
        GameCopy copy = new GameCopy(game);

        copy.rent();

        assertThat(copy.getStatus()).isEqualTo(GameCopyStatus.RENTED);
    }

    @Test
    void rentedCopyCannotBeRentedAgain() {
        GameCopy copy = new GameCopy(game);
        copy.rent();

        assertThatThrownBy(copy::rent)
                .isInstanceOf(InvalidGameCopyStatusException.class)
                .hasMessageContaining("RENTED");
    }

    @Test
    void lostCopyCannotBeRented() {
        GameCopy copy = new GameCopy(game);
        copy.markAsLost();

        assertThatThrownBy(copy::rent)
                .isInstanceOf(InvalidGameCopyStatusException.class)
                .hasMessageContaining("LOST");
    }

    @Test
    void damagedCopyCannotBeRented() {
        GameCopy copy = new GameCopy(game);
        copy.markAsDamaged();

        assertThatThrownBy(copy::rent)
                .isInstanceOf(InvalidGameCopyStatusException.class)
                .hasMessageContaining("DAMAGED");
    }

    @Test
    void rentedCopyCanBeMarkedAsReturned() {
        GameCopy copy = new GameCopy(game);
        copy.rent();

        copy.markAsReturned();

        assertThat(copy.getStatus()).isEqualTo(GameCopyStatus.RETURNED);
    }

    @Test
    void availableCopyCannotBeMarkedAsReturned() {
        GameCopy copy = new GameCopy(game);

        assertThatThrownBy(copy::markAsReturned)
                .isInstanceOf(InvalidGameCopyStatusException.class)
                .hasMessageContaining("AVAILABLE");
    }
}
