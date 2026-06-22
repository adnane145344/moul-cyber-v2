package com.adnane.moulcyber.infra.persistence.rental;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItem;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void canPersistRentalWithProcessedItem() {
        User customer = userRepository.saveAndFlush(customer());
        GameCopy copy = savedCopy();
        Rental rental = new Rental(
                customer,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 8));
        RentalItem item = new RentalItem(copy, copy.getGame().getRentalPrice());
        rental.addItem(item);
        copy.rent();
        item.returnOn(LocalDate.of(2026, 6, 10));

        Rental savedRental = rentalRepository.saveAndFlush(rental);
        Long rentalId = savedRental.getId();
        entityManager.clear();

        Rental reloadedRental = rentalRepository.findById(rentalId).orElseThrow();
        assertThat(reloadedRental.getStatus()).isEqualTo(
                com.adnane.moulcyber.domain.rental.RentalStatus.COMPLETED);
        assertThat(reloadedRental.getItems())
                .singleElement()
                .satisfies(reloadedItem -> {
                    assertThat(reloadedItem.getId()).isNotNull();
                    assertThat(reloadedItem.getGameCopy().getId()).isEqualTo(copy.getId());
                    assertThat(reloadedItem.getRental().getId()).isEqualTo(rentalId);
                    assertThat(reloadedItem.getStatus()).isEqualTo(
                            com.adnane.moulcyber.domain.rental.RentalItemStatus.LATE_RETURNED);
                    assertThat(reloadedItem.getProcessedDate()).isEqualTo(LocalDate.of(2026, 6, 10));
                    assertThat(reloadedItem.getRentalPrice()).isEqualByComparingTo("5.00");
                    assertThat(reloadedItem.getLateFee()).isEqualByComparingTo("4.00");
                });
    }

    @Test
    void canFindRentalHistoryNewestFirst() {
        User customer = userRepository.saveAndFlush(customer());
        rentalRepository.save(new Rental(
                customer,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 8)));
        rentalRepository.saveAndFlush(new Rental(
                customer,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 8)));

        assertThat(rentalRepository.findByUserId(customer.getId())).hasSize(2);
        assertThat(rentalRepository.findDistinctByUserIdOrderByStartDateDescIdDesc(customer.getId()))
                .extracting(Rental::getStartDate)
                .containsExactly(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 1));
    }

    private User customer() {
        return new User(
                "Adnane",
                "Lardi",
                "rental.customer@example.com",
                "password-hash",
                Role.CLIENT);
    }

    private GameCopy savedCopy() {
        Game game = gameRepository.saveAndFlush(new Game(
                "Cyber Quest",
                "A cooperative science-fiction adventure.",
                new BigDecimal("5.00")));
        return gameCopyRepository.saveAndFlush(new GameCopy(game));
    }
}
