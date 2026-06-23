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
import com.adnane.moulcyber.application.shared.PageResponse;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.rental.RentalItemStatus;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItem;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.rental.RentalRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
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
    void returnsPagedRentalsInRepositoryOrder() {
        Rental active = rental(10L, TODAY, TODAY.plusDays(7));
        Rental overdue = rental(11L, TODAY.minusDays(10), TODAY.minusDays(3));
        Pageable pageable = PageRequest.of(0, 2);
        when(rentalRepository.findAllIds(pageable))
                .thenReturn(new PageImpl<>(List.of(11L, 10L), pageable, 3));
        when(rentalRepository.findDetailedByIdIn(List.of(11L, 10L)))
                .thenReturn(List.of(active, overdue));

        PageResponse<AdminRentalSummaryResponse> response = service.findRentals(null, pageable);

        assertThat(response.content())
                .extracting(AdminRentalSummaryResponse::id)
                .containsExactly(11L, 10L);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(2);
    }

    @Test
    void delegatesActiveOverdueAndCompletedFilteringToRepository() {
        Pageable pageable = PageRequest.of(1, 5);
        when(rentalRepository.findActiveIds(RentalItemStatus.ACTIVE, TODAY, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(rentalRepository.findOverdueIds(RentalItemStatus.ACTIVE, TODAY, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(rentalRepository.findCompletedIds(RentalItemStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.findRentals(AdminRentalFilter.ACTIVE, pageable);
        service.findRentals(AdminRentalFilter.OVERDUE, pageable);
        service.findRentals(AdminRentalFilter.COMPLETED, pageable);

        verify(rentalRepository).findActiveIds(RentalItemStatus.ACTIVE, TODAY, pageable);
        verify(rentalRepository).findOverdueIds(RentalItemStatus.ACTIVE, TODAY, pageable);
        verify(rentalRepository).findCompletedIds(RentalItemStatus.ACTIVE, pageable);
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
