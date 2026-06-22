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
    void canPersistRentalWithItemsAndReturnDate() {
        User customer = userRepository.saveAndFlush(customer());
        GameCopy copy = savedCopy();
        Rental rental = new Rental(
                customer,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 8));
        rental.addItem(new RentalItem(copy));
        rental.returnOn(LocalDate.of(2026, 6, 10));

        Rental savedRental = rentalRepository.saveAndFlush(rental);
        Long rentalId = savedRental.getId();
        entityManager.clear();

        Rental reloadedRental = rentalRepository.findById(rentalId).orElseThrow();
        assertThat(reloadedRental.getReturnedDate()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(reloadedRental.calculateLateFee()).isEqualByComparingTo("4.00");
        assertThat(reloadedRental.getItems())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getId()).isNotNull();
                    assertThat(item.getGameCopy().getId()).isEqualTo(copy.getId());
                    assertThat(item.getRental().getId()).isEqualTo(rentalId);
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
        assertThat(rentalRepository.findByUserIdOrderByStartDateDesc(customer.getId()))
                .extracting(Rental::getStartDate)
                .containsExactly(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 1));
    }

    private User customer() {
        return new User(
                "Adnane",
                "Lardi",
                "rental.customer@example.com",
                "password-hash",
                Role.CUSTOMER);
    }

    private GameCopy savedCopy() {
        Game game = gameRepository.saveAndFlush(new Game(
                "Cyber Quest",
                "A cooperative science-fiction adventure.",
                new BigDecimal("5.00")));
        return gameCopyRepository.saveAndFlush(new GameCopy(game));
    }
}
