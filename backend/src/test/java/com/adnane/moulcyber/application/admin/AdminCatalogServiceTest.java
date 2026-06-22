package com.adnane.moulcyber.application.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.adnane.moulcyber.application.catalog.GameNotFoundException;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCatalogServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameCopyRepository gameCopyRepository;

    private AdminCatalogService service;

    @BeforeEach
    void setUp() {
        service = new AdminCatalogService(
                gameRepository,
                gameCopyRepository,
                new AdminCatalogAssembler());
    }

    @Test
    void createsTrimmedGame() {
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game pending = invocation.getArgument(0);
            return new Game(
                    10L,
                    pending.getTitle(),
                    pending.getDescription(),
                    pending.getRentalPrice());
        });

        AdminGameResponse response = service.createGame(new CreateGameRequest(
                "  Cyber Quest  ",
                "  Cooperative adventure.  ",
                new BigDecimal("5.00")));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Cyber Quest");
        assertThat(response.description()).isEqualTo("Cooperative adventure.");
        assertThat(response.rentalPrice()).isEqualByComparingTo("5.00");
    }

    @Test
    void updatesExistingGameWithoutReplacingEntity() {
        Game game = new Game(
                10L, "Old title", "Old description.", new BigDecimal("4.00"));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));

        AdminGameResponse response = service.updateGame(10L, new UpdateGameRequest(
                "New title", "New description.", new BigDecimal("6.50")));

        assertThat(response.title()).isEqualTo("New title");
        assertThat(game.getDescription()).isEqualTo("New description.");
        assertThat(game.getRentalPrice()).isEqualByComparingTo("6.50");
    }

    @Test
    void addsRequestedAvailableCopies() {
        Game game = new Game(
                10L, "Cyber Quest", "Adventure.", new BigDecimal("5.00"));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(gameCopyRepository.countByGameId(10L)).thenReturn(7L);

        AddGameCopiesResponse response = service.addCopies(
                10L, new AddGameCopiesRequest(3));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GameCopy>> copiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(gameCopyRepository).saveAll(copiesCaptor.capture());
        assertThat(copiesCaptor.getValue())
                .hasSize(3)
                .allSatisfy(copy -> {
                    assertThat(copy.getGame()).isSameAs(game);
                    assertThat(copy.getStatus()).isEqualTo(GameCopyStatus.AVAILABLE);
                });
        assertThat(response.totalCopies()).isEqualTo(7);
    }

    @Test
    void missingGameFailsForUpdateAndCopyCreation() {
        when(gameRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateGame(
                404L,
                new UpdateGameRequest(
                        "Title", "Description.", new BigDecimal("5.00"))))
                .isInstanceOf(GameNotFoundException.class);
        assertThatThrownBy(() -> service.addCopies(
                404L, new AddGameCopiesRequest(1)))
                .isInstanceOf(GameNotFoundException.class);
    }
}
