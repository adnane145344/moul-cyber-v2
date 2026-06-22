package com.adnane.moulcyber.application.admin;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyStatusCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminInventoryService {

    private final GameRepository gameRepository;
    private final GameCopyRepository gameCopyRepository;

    public AdminInventoryService(
            GameRepository gameRepository,
            GameCopyRepository gameCopyRepository) {
        this.gameRepository = gameRepository;
        this.gameCopyRepository = gameCopyRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryGameResponse> findInventory() {
        Map<Long, Map<GameCopyStatus, Long>> countsByGame = new HashMap<>();
        for (GameCopyStatusCount count : gameCopyRepository.countCopiesGroupedByGameAndStatus()) {
            countsByGame.computeIfAbsent(
                            count.getGameId(),
                            ignored -> new EnumMap<>(GameCopyStatus.class))
                    .put(count.getStatus(), count.getCopyCount());
        }

        return gameRepository.findAllByOrderByTitleAscIdAsc().stream()
                .map(game -> response(
                        game.getId(),
                        game.getTitle(),
                        countsByGame.getOrDefault(
                                game.getId(),
                                Map.of())))
                .toList();
    }

    private InventoryGameResponse response(
            Long gameId,
            String title,
            Map<GameCopyStatus, Long> counts) {
        long available = counts.getOrDefault(GameCopyStatus.AVAILABLE, 0L);
        long rented = counts.getOrDefault(GameCopyStatus.RENTED, 0L);
        long lost = counts.getOrDefault(GameCopyStatus.LOST, 0L);
        long damaged = counts.getOrDefault(GameCopyStatus.DAMAGED, 0L);
        return new InventoryGameResponse(
                gameId,
                title,
                available,
                rented,
                lost,
                damaged,
                available + rented + lost + damaged);
    }
}
