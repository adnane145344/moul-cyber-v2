package com.adnane.moulcyber.application.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameAvailabilityCount;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameCopyRepository gameCopyRepository;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(
                gameRepository,
                gameCopyRepository,
                new GameCatalogAssembler());
    }

    @Test
    void returnsFullCatalogWithAvailabilityUsingOneGroupedQuery() {
        Game cyberQuest = game(1L, "Cyber Quest");
        Game spaceStrategy = game(2L, "Space Strategy");
        when(gameRepository.findAllByOrderByTitleAscIdAsc())
                .thenReturn(List.of(cyberQuest, spaceStrategy));
        when(gameCopyRepository.countAvailabilityByGameIds(
                List.of(1L, 2L), GameCopyStatus.AVAILABLE))
                .thenReturn(List.of(availability(1L, 2)));

        List<GameSummaryResponse> result = catalogService.findGames(null);

        assertThat(result)
                .extracting(GameSummaryResponse::title)
                .containsExactly("Cyber Quest", "Space Strategy");
        assertThat(result)
                .extracting(GameSummaryResponse::availableCopies)
                .containsExactly(2L, 0L);
        verify(gameCopyRepository).countAvailabilityByGameIds(
                List.of(1L, 2L), GameCopyStatus.AVAILABLE);
    }

    @Test
    void trimsTitleAndUsesCaseInsensitiveRepositorySearch() {
        Game cyberQuest = game(1L, "Cyber Quest");
        when(gameRepository.findByTitleContainingIgnoreCaseOrderByTitleAscIdAsc("CYBER"))
                .thenReturn(List.of(cyberQuest));
        when(gameCopyRepository.countAvailabilityByGameIds(
                List.of(1L), GameCopyStatus.AVAILABLE))
                .thenReturn(List.of());

        List<GameSummaryResponse> result = catalogService.findGames("  CYBER  ");

        assertThat(result).singleElement()
                .satisfies(game -> assertThat(game.title()).isEqualTo("Cyber Quest"));
    }

    @Test
    void blankTitleReturnsFullCatalog() {
        when(gameRepository.findAllByOrderByTitleAscIdAsc()).thenReturn(List.of());

        assertThat(catalogService.findGames("   ")).isEmpty();

        verify(gameRepository).findAllByOrderByTitleAscIdAsc();
        verify(gameCopyRepository, never()).countAvailabilityByGameIds(
                List.of(), GameCopyStatus.AVAILABLE);
    }

    @Test
    void noMatchingGamesReturnsEmptyListWithoutAvailabilityQuery() {
        when(gameRepository.findByTitleContainingIgnoreCaseOrderByTitleAscIdAsc("unknown"))
                .thenReturn(List.of());

        assertThat(catalogService.findGames("unknown")).isEmpty();

        verify(gameCopyRepository, never()).countAvailabilityByGameIds(
                List.of(), GameCopyStatus.AVAILABLE);
    }

    @Test
    void detailContainsDescriptionAndAvailability() {
        Game game = game(7L, "Cyber Quest");
        when(gameRepository.findById(7L)).thenReturn(Optional.of(game));
        when(gameCopyRepository.countByGameIdAndStatus(7L, GameCopyStatus.AVAILABLE))
                .thenReturn(3L);

        GameDetailsResponse result = catalogService.findGame(7L);

        assertThat(result.description()).isEqualTo("A video game description.");
        assertThat(result.availableCopies()).isEqualTo(3);
    }

    @Test
    void missingGameThrowsGameNotFoundException() {
        when(gameRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.findGame(404L))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessage("Game not found.");

        verify(gameCopyRepository, never())
                .countByGameIdAndStatus(404L, GameCopyStatus.AVAILABLE);
    }

    private Game game(Long id, String title) {
        return new Game(id, title, "A video game description.", new BigDecimal("5.00"));
    }

    private GameAvailabilityCount availability(Long gameId, long availableCopies) {
        return new GameAvailabilityCount() {
            @Override
            public Long getGameId() {
                return gameId;
            }

            @Override
            public long getAvailableCopies() {
                return availableCopies;
            }
        };
    }
}
