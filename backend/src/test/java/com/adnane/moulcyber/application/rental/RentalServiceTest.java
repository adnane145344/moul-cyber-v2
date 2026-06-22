package com.adnane.moulcyber.application.rental;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.adnane.moulcyber.application.catalog.GameNotFoundException;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItemStatus;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import com.adnane.moulcyber.infra.persistence.rental.RentalRepository;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-06-22T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameCopyRepository gameCopyRepository;
    @Mock
    private RentalRepository rentalRepository;

    private RentalService rentalService;
    private User user;
    private Game game;
    private GameCopy copy;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService(
                userRepository,
                gameRepository,
                gameCopyRepository,
                rentalRepository,
                new RentalAssembler(),
                CLOCK);
        user = new User(1L, "Adnane", "Lardi", "client@example.com",
                "password-hash", Role.CLIENT);
        game = new Game(2L, "Cyber Quest", "Adventure.", new BigDecimal("5.00"));
        copy = new GameCopy(3L, game, GameCopyStatus.AVAILABLE);
    }

    @Test
    void createsRentalAndRentsLockedCopy() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(2L)).thenReturn(Optional.of(game));
        when(gameCopyRepository.findFirstByGameIdAndStatusOrderByIdAsc(
                2L, GameCopyStatus.AVAILABLE)).thenReturn(Optional.of(copy));
        when(rentalRepository.saveAndFlush(any(Rental.class))).thenAnswer(invocation -> {
            Rental pendingRental = invocation.getArgument(0);
            Rental savedRental = new Rental(
                    10L,
                    pendingRental.getUser(),
                    pendingRental.getStartDate(),
                    pendingRental.getDueDate());
            savedRental.addItem(new com.adnane.moulcyber.domain.rental.RentalItem(
                    22L, copy, game.getRentalPrice()));
            return savedRental;
        });

        RentalResponse response = rentalService.createRental(
                1L, new CreateRentalRequest(2L));

        assertThat(copy.getStatus()).isEqualTo(GameCopyStatus.RENTED);
        assertThat(response.startDate()).isEqualTo("2026-06-22");
        assertThat(response.dueDate()).isEqualTo("2026-06-29");
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.status()).isEqualTo(RentalItemStatus.ACTIVE);
            assertThat(item.rentalPrice()).isEqualByComparingTo("5.00");
        });
    }

    @Test
    void missingGameFailsWithoutLockingCopy() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.createRental(
                1L, new CreateRentalRequest(404L)))
                .isInstanceOf(GameNotFoundException.class);

        verify(gameCopyRepository, never())
                .findFirstByGameIdAndStatusOrderByIdAsc(any(), any());
    }

    @Test
    void unavailableGameFailsWithoutSavingRental() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(2L)).thenReturn(Optional.of(game));
        when(gameCopyRepository.findFirstByGameIdAndStatusOrderByIdAsc(
                2L, GameCopyStatus.AVAILABLE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.createRental(
                1L, new CreateRentalRequest(2L)))
                .isInstanceOf(GameUnavailableException.class);

        verify(rentalRepository, never()).saveAndFlush(any());
    }

    @Test
    void returnsOnlyCurrentUserHistory() {
        Rental rental = new Rental(10L, user,
                java.time.LocalDate.of(2026, 6, 22),
                java.time.LocalDate.of(2026, 6, 29));
        rental.addItem(new com.adnane.moulcyber.domain.rental.RentalItem(
                22L, copy, game.getRentalPrice()));
        when(rentalRepository.findDistinctByUserIdOrderByStartDateDescIdDesc(1L))
                .thenReturn(List.of(rental));

        assertThat(rentalService.findUserRentals(1L))
                .singleElement()
                .satisfies(summary -> assertThat(summary.id()).isEqualTo(10L));
    }

    @Test
    void cannotReadAnotherUsersRental() {
        when(rentalRepository.findDistinctByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.findUserRental(1L, 10L))
                .isInstanceOf(RentalNotFoundException.class);
    }
}
