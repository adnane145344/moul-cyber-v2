package com.adnane.moulcyber.application.rental;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItem;
import com.adnane.moulcyber.domain.rental.RentalItemStatus;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.rental.RentalItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalItemProcessingServiceTest {

    private static final LocalDate PROCESSING_DATE = LocalDate.of(2026, 6, 22);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-06-22T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private RentalItemRepository rentalItemRepository;

    private RentalItemProcessingService service;

    @BeforeEach
    void setUp() {
        service = new RentalItemProcessingService(rentalItemRepository, CLOCK);
    }

    @Test
    void normalReturnMakesCopyAvailable() {
        RentalItem item = activeItem(LocalDate.of(2026, 6, 22));
        when(rentalItemRepository.findDetailsById(22L)).thenReturn(Optional.of(item));

        RentalItemProcessingResponse response = service.returnItem(22L);

        assertThat(response.status()).isEqualTo(RentalItemStatus.RETURNED);
        assertThat(response.copyStatus()).isEqualTo(GameCopyStatus.AVAILABLE);
        assertThat(response.lateFee()).isEqualByComparingTo("0.00");
    }

    @Test
    void lateReturnCalculatesLateFee() {
        RentalItem item = activeItem(LocalDate.of(2026, 6, 19));
        when(rentalItemRepository.findDetailsById(22L)).thenReturn(Optional.of(item));

        RentalItemProcessingResponse response = service.returnItem(22L);

        assertThat(response.status()).isEqualTo(RentalItemStatus.LATE_RETURNED);
        assertThat(response.lateFee()).isEqualByComparingTo("6.00");
    }

    @Test
    void lostAndDamagedCopiesRemainUnavailable() {
        RentalItem lost = activeItem(LocalDate.of(2026, 6, 29));
        RentalItem damaged = activeItem(LocalDate.of(2026, 6, 29));
        when(rentalItemRepository.findDetailsById(22L)).thenReturn(Optional.of(lost));
        when(rentalItemRepository.findDetailsById(23L)).thenReturn(Optional.of(damaged));

        assertThat(service.markLost(22L).copyStatus()).isEqualTo(GameCopyStatus.LOST);
        assertThat(service.markDamaged(23L).copyStatus()).isEqualTo(GameCopyStatus.DAMAGED);
    }

    @Test
    void missingAndCompletedItemsFail() {
        when(rentalItemRepository.findDetailsById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.returnItem(404L))
                .isInstanceOf(RentalItemNotFoundException.class);

        RentalItem completed = activeItem(LocalDate.of(2026, 6, 29));
        completed.returnOn(PROCESSING_DATE);
        when(rentalItemRepository.findDetailsById(22L)).thenReturn(Optional.of(completed));
        assertThatThrownBy(() -> service.markLost(22L))
                .isInstanceOf(RentalItemAlreadyProcessedException.class);
    }

    private RentalItem activeItem(LocalDate dueDate) {
        User user = new User(1L, "Adnane", "Lardi", "client@example.com",
                "password-hash", Role.CLIENT);
        Game game = new Game(2L, "Cyber Quest", "Adventure.", new BigDecimal("5.00"));
        GameCopy copy = new GameCopy(3L, game, GameCopyStatus.AVAILABLE);
        copy.rent();
        Rental rental = new Rental(10L, user, LocalDate.of(2026, 6, 15), dueDate);
        RentalItem item = new RentalItem(22L, copy, game.getRentalPrice());
        rental.addItem(item);
        return item;
    }
}
