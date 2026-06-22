package com.adnane.moulcyber.application.admin;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.adnane.moulcyber.application.rental.RentalAssembler;
import com.adnane.moulcyber.application.rental.RentalNotFoundException;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItem;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.rental.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRentalServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 22);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-06-22T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private RentalRepository rentalRepository;

    private AdminRentalService service;
    private User user;
    private Game game;

    @BeforeEach
    void setUp() {
        service = new AdminRentalService(
                rentalRepository,
                new AdminRentalAssembler(new RentalAssembler()),
                CLOCK);
        user = new User(
                1L, "Client", "User", "client@example.com", "hash", Role.CLIENT);
        game = new Game(
                2L, "Cyber Quest", "Adventure.", new BigDecimal("5.00"));
    }

    @Test
    void filtersActiveOverdueAndCompletedRentals() {
        Rental active = rental(10L, TODAY, TODAY.plusDays(7));
        Rental overdue = rental(11L, TODAY.minusDays(10), TODAY.minusDays(3));
        Rental completed = rental(12L, TODAY.minusDays(9), TODAY.minusDays(2));
        completed.getItems().getFirst().returnOn(TODAY.minusDays(2));
        when(rentalRepository.findDistinctByOrderByStartDateDescIdDesc())
                .thenReturn(List.of(active, overdue, completed));

        assertThat(service.findRentals(AdminRentalFilter.ACTIVE))
                .extracting(AdminRentalSummaryResponse::id)
                .containsExactly(10L);
        assertThat(service.findRentals(AdminRentalFilter.OVERDUE))
                .extracting(AdminRentalSummaryResponse::id)
                .containsExactly(11L);
        assertThat(service.findRentals(AdminRentalFilter.COMPLETED))
                .extracting(AdminRentalSummaryResponse::id)
                .containsExactly(12L);
        assertThat(service.findRentals(null)).hasSize(3);
    }

    @Test
    void returnsAdministrativeDetailsWithCustomerAndItems() {
        Rental rental = rental(10L, TODAY, TODAY.plusDays(7));
        when(rentalRepository.findDetailedById(10L)).thenReturn(Optional.of(rental));

        AdminRentalDetailsResponse response = service.findRental(10L);

        assertThat(response.customerName()).isEqualTo("Client User");
        assertThat(response.customerEmail()).isEqualTo("client@example.com");
        assertThat(response.overdue()).isFalse();
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void missingRentalFails() {
        when(rentalRepository.findDetailedById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findRental(404L))
                .isInstanceOf(RentalNotFoundException.class);
    }

    private Rental rental(Long id, LocalDate start, LocalDate due) {
        GameCopy copy = new GameCopy(id + 100, game, com.adnane.moulcyber.domain.inventory.GameCopyStatus.RENTED);
        Rental rental = new Rental(id, user, start, due);
        rental.addItem(new RentalItem(id + 200, copy, game.getRentalPrice()));
        return rental;
    }
}
