package com.adnane.moulcyber.infra.persistence.catalog;

import java.math.BigDecimal;

import com.adnane.moulcyber.domain.catalog.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    @Test
    void canSaveGameAndPreserveRentalPrice() {
        Game savedGame = gameRepository.saveAndFlush(game("Cyber Quest", "5.25"));

        assertThat(savedGame.getId()).isNotNull();
        assertThat(gameRepository.findById(savedGame.getId()))
                .hasValueSatisfying(game ->
                        assertThat(game.getRentalPrice()).isEqualByComparingTo("5.25"));
    }

    @Test
    void canFindGamesByPartialTitleIgnoringCase() {
        gameRepository.save(game("Cyber Quest", "5.00"));
        gameRepository.save(game("Space Strategy", "4.50"));
        gameRepository.flush();

        assertThat(gameRepository.findByTitleContainingIgnoreCase("CYBER"))
                .extracting(Game::getTitle)
                .containsExactly("Cyber Quest");
    }

    private Game game(String title, String price) {
        return new Game(title, "A video game description.", new BigDecimal(price));
    }
}
