package com.adnane.moulcyber.api.catalog;

import java.math.BigDecimal;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import com.adnane.moulcyber.support.PostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GameCatalogApiTest extends PostgreSQLContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @BeforeEach
    void cleanDatabase() {
        gameCopyRepository.deleteAll();
        gameRepository.deleteAll();
    }

    @Test
    void anonymousCanListCatalogInStableOrder() throws Exception {
        Game spaceStrategy = saveGame("Space Strategy", "A strategy game.", "4.50");
        Game cyberQuest = saveGame("Cyber Quest", "A cooperative adventure.", "5.00");
        saveAvailableCopy(cyberQuest);
        saveAvailableCopy(cyberQuest);
        saveRentedCopy(cyberQuest);
        saveDamagedCopy(spaceStrategy);

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(cyberQuest.getId()))
                .andExpect(jsonPath("$[0].title").value("Cyber Quest"))
                .andExpect(jsonPath("$[0].rentalPrice").value(5.00))
                .andExpect(jsonPath("$[0].availableCopies").value(2))
                .andExpect(jsonPath("$[0].description").doesNotExist())
                .andExpect(jsonPath("$[0].gameCopies").doesNotExist())
                .andExpect(jsonPath("$[0].status").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(spaceStrategy.getId()))
                .andExpect(jsonPath("$[1].availableCopies").value(0));
    }

    @Test
    void anonymousCanSearchByTrimmedCaseInsensitiveTitle() throws Exception {
        saveGame("Cyber Quest", "A cooperative adventure.", "5.00");
        saveGame("Space Strategy", "A strategy game.", "4.50");

        mockMvc.perform(get("/api/games").param("title", "  CYBER  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Cyber Quest"));
    }

    @Test
    void blankSearchReturnsFullCatalogAndUnknownSearchReturnsEmptyList() throws Exception {
        saveGame("Cyber Quest", "A cooperative adventure.", "5.00");

        mockMvc.perform(get("/api/games").param("title", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/games").param("title", "unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void anonymousCanReadGameDetails() throws Exception {
        Game game = saveGame("Cyber Quest", "A cooperative adventure.", "5.00");
        saveAvailableCopy(game);
        saveLostCopy(game);

        mockMvc.perform(get("/api/games/{gameId}", game.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(game.getId()))
                .andExpect(jsonPath("$.title").value("Cyber Quest"))
                .andExpect(jsonPath("$.description").value("A cooperative adventure."))
                .andExpect(jsonPath("$.rentalPrice").value(5.00))
                .andExpect(jsonPath("$.availableCopies").value(1))
                .andExpect(jsonPath("$.gameCopies").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @Test
    void missingGameReturnsNotFoundApiError() throws Exception {
        mockMvc.perform(get("/api/games/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Game not found."))
                .andExpect(jsonPath("$.path").value("/api/games/999999"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    private Game saveGame(String title, String description, String price) {
        return gameRepository.saveAndFlush(
                new Game(title, description, new BigDecimal(price)));
    }

    private void saveAvailableCopy(Game game) {
        gameCopyRepository.save(new GameCopy(game));
    }

    private void saveRentedCopy(Game game) {
        GameCopy copy = new GameCopy(game);
        copy.rent();
        gameCopyRepository.save(copy);
    }

    private void saveLostCopy(Game game) {
        GameCopy copy = new GameCopy(game);
        copy.markAsLost();
        gameCopyRepository.save(copy);
    }

    private void saveDamagedCopy(Game game) {
        GameCopy copy = new GameCopy(game);
        copy.markAsDamaged();
        gameCopyRepository.save(copy);
    }
}
