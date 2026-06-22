package com.adnane.moulcyber.application.admin;

import java.math.BigDecimal;
import java.util.List;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyStatusCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInventoryServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameCopyRepository gameCopyRepository;

    @Test
    void assemblesAllStatusesAndKeepsGamesWithoutCopies() {
        Game cyber = game(1L, "Cyber Quest");
        Game empty = game(2L, "Empty Game");
        when(gameRepository.findAllByOrderByTitleAscIdAsc())
                .thenReturn(List.of(cyber, empty));
        when(gameCopyRepository.countCopiesGroupedByGameAndStatus())
                .thenReturn(List.of(
                        count(1L, GameCopyStatus.AVAILABLE, 2),
                        count(1L, GameCopyStatus.RENTED, 1),
                        count(1L, GameCopyStatus.LOST, 3),
                        count(1L, GameCopyStatus.DAMAGED, 4)));

        List<InventoryGameResponse> inventory = new AdminInventoryService(
                gameRepository, gameCopyRepository).findInventory();

        assertThat(inventory).hasSize(2);
        assertThat(inventory.getFirst()).isEqualTo(new InventoryGameResponse(
                1L, "Cyber Quest", 2, 1, 3, 4, 10));
        assertThat(inventory.getLast()).isEqualTo(new InventoryGameResponse(
                2L, "Empty Game", 0, 0, 0, 0, 0));
    }

    private Game game(Long id, String title) {
        return new Game(id, title, "Description.", new BigDecimal("5.00"));
    }

    private GameCopyStatusCount count(
            Long gameId,
            GameCopyStatus status,
            long copyCount) {
        return new GameCopyStatusCount() {
            @Override
            public Long getGameId() {
                return gameId;
            }

            @Override
            public GameCopyStatus getStatus() {
                return status;
            }

            @Override
            public long getCopyCount() {
                return copyCount;
            }
        };
    }
}
