package com.adnane.moulcyber.application.catalog;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameAvailabilityCount;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final GameRepository gameRepository;
    private final GameCopyRepository gameCopyRepository;
    private final GameCatalogAssembler assembler;

    public CatalogService(
            GameRepository gameRepository,
            GameCopyRepository gameCopyRepository,
            GameCatalogAssembler assembler) {
        this.gameRepository = gameRepository;
        this.gameCopyRepository = gameCopyRepository;
        this.assembler = assembler;
    }

    @Transactional(readOnly = true)
    public List<GameSummaryResponse> findGames(String title) {
        String normalizedTitle = normalizeTitle(title);
        List<Game> games = normalizedTitle == null
                ? gameRepository.findAllByOrderByTitleAscIdAsc()
                : gameRepository.findByTitleContainingIgnoreCaseOrderByTitleAscIdAsc(normalizedTitle);

        if (games.isEmpty()) {
            return List.of();
        }

        List<Long> gameIds = games.stream().map(Game::getId).toList();
        Map<Long, Long> availability = gameCopyRepository
                .countAvailabilityByGameIds(gameIds, GameCopyStatus.AVAILABLE)
                .stream()
                .collect(Collectors.toMap(
                        GameAvailabilityCount::getGameId,
                        GameAvailabilityCount::getAvailableCopies));

        return games.stream()
                .map(game -> assembler.toSummary(
                        game,
                        availability.getOrDefault(game.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public GameDetailsResponse findGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(GameNotFoundException::new);
        long availableCopies = gameCopyRepository.countByGameIdAndStatus(
                gameId,
                GameCopyStatus.AVAILABLE);
        return assembler.toDetails(game, availableCopies);
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title.trim();
    }
}
