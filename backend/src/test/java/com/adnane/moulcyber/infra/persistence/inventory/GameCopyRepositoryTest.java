package com.adnane.moulcyber.infra.persistence.inventory;

import java.math.BigDecimal;
import java.util.List;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
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
class GameCopyRepositoryTest {

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void canFindAndCountAvailableCopiesForGame() {
        Game game = gameRepository.saveAndFlush(game());
        gameCopyRepository.save(new GameCopy(game));
        GameCopy rentedCopy = new GameCopy(game);
        rentedCopy.rent();
        gameCopyRepository.saveAndFlush(rentedCopy);

        assertThat(gameCopyRepository.findByGameIdAndStatus(game.getId(), GameCopyStatus.AVAILABLE))
                .hasSize(1)
                .allSatisfy(copy -> assertThat(copy.getStatus()).isEqualTo(GameCopyStatus.AVAILABLE));
        assertThat(gameCopyRepository.countByGameIdAndStatus(game.getId(), GameCopyStatus.RENTED))
                .isEqualTo(1);
    }

    @Test
    void persistsGameCopyStatusTransitions() {
        Game game = gameRepository.saveAndFlush(game());
        GameCopy copy = gameCopyRepository.saveAndFlush(new GameCopy(game));

        copy.rent();
        gameCopyRepository.flush();
        entityManager.clear();

        GameCopy rentedCopy = gameCopyRepository.findById(copy.getId()).orElseThrow();
        assertThat(rentedCopy.getStatus()).isEqualTo(GameCopyStatus.RENTED);

        rentedCopy.markAsReturned();
        gameCopyRepository.flush();
        entityManager.clear();

        assertThat(gameCopyRepository.findById(copy.getId()).orElseThrow().getStatus())
                .isEqualTo(GameCopyStatus.AVAILABLE);
    }

    @Test
    void groupedAvailabilityCountsOnlyAvailableCopies() {
        Game availableGame = gameRepository.saveAndFlush(game("Available Game"));
        Game unavailableGame = gameRepository.saveAndFlush(game("Unavailable Game"));
        gameCopyRepository.save(new GameCopy(availableGame));
        gameCopyRepository.save(new GameCopy(availableGame));
        gameCopyRepository.save(copyWithStatus(availableGame, GameCopyStatus.RENTED));
        gameCopyRepository.save(copyWithStatus(availableGame, GameCopyStatus.LOST));
        gameCopyRepository.save(copyWithStatus(availableGame, GameCopyStatus.DAMAGED));
        gameCopyRepository.saveAndFlush(copyWithStatus(unavailableGame, GameCopyStatus.RENTED));

        assertThat(gameCopyRepository.countAvailabilityByGameIds(
                List.of(availableGame.getId(), unavailableGame.getId()),
                GameCopyStatus.AVAILABLE))
                .singleElement()
                .satisfies(count -> {
                    assertThat(count.getGameId()).isEqualTo(availableGame.getId());
                    assertThat(count.getAvailableCopies()).isEqualTo(2);
                });
    }

    private Game game() {
        return game("Cyber Quest");
    }

    private Game game(String title) {
        return new Game(
                title,
                "A cooperative science-fiction adventure.",
                new BigDecimal("5.00"));
    }

    private GameCopy copyWithStatus(Game game, GameCopyStatus status) {
        GameCopy copy = new GameCopy(game);
        switch (status) {
            case AVAILABLE -> {
            }
            case RENTED -> copy.rent();
            case LOST -> copy.markAsLost();
            case DAMAGED -> copy.markAsDamaged();
        }
        return copy;
    }
}
