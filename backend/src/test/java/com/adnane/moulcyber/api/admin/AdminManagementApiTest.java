package com.adnane.moulcyber.api.admin;

import java.math.BigDecimal;

import com.adnane.moulcyber.configuration.security.JwtService;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import com.adnane.moulcyber.support.PostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminManagementApiTest extends PostgreSQLContainerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameCopyRepository gameCopyRepository;
    @Autowired
    private JwtService jwtService;

    private User admin;
    private User client;

    @BeforeEach
    void setUp() {
        admin = userRepository.saveAndFlush(new User(
                "Admin", "User", "admin.management@example.com", "hash", Role.ADMIN));
        client = userRepository.saveAndFlush(new User(
                "Client", "User", "client.management@example.com", "hash", Role.CLIENT));
    }

    @Test
    void adminCanCreateUpdateAndAddCopies() throws Exception {
        String createResponse = mockMvc.perform(post("/api/admin/games")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "  Cyber Quest  ",
                                  "description": "  Cooperative adventure.  ",
                                  "rentalPrice": 5.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Cyber Quest"))
                .andExpect(jsonPath("$.description").value("Cooperative adventure."))
                .andExpect(jsonPath("$.rentalPrice").value(5.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long gameId = gameRepository.findByTitleContainingIgnoreCaseOrderByTitleAscIdAsc("Cyber")
                .getFirst()
                .getId();
        assertThat(createResponse).contains(gameId.toString());

        mockMvc.perform(put("/api/admin/games/{gameId}", gameId)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cyber Quest Deluxe",
                                  "description": "Updated adventure.",
                                  "rentalPrice": 7.50
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Cyber Quest Deluxe"))
                .andExpect(jsonPath("$.rentalPrice").value(7.50));

        mockMvc.perform(post("/api/admin/games/{gameId}/copies", gameId)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.addedCopies").value(3))
                .andExpect(jsonPath("$.totalCopies").value(3));
    }

    @Test
    void inventoryContainsGroupedStatusesAndGamesWithoutCopies() throws Exception {
        Game populated = gameRepository.saveAndFlush(new Game(
                "Cyber Quest", "Adventure.", new BigDecimal("5.00")));
        Game empty = gameRepository.saveAndFlush(new Game(
                "Empty Game", "No copies.", new BigDecimal("4.00")));
        gameCopyRepository.save(new GameCopy(populated));
        GameCopy rented = new GameCopy(populated);
        rented.rent();
        gameCopyRepository.save(rented);
        GameCopy lost = new GameCopy(populated);
        lost.markAsLost();
        gameCopyRepository.save(lost);
        GameCopy damaged = new GameCopy(populated);
        damaged.markAsDamaged();
        gameCopyRepository.saveAndFlush(damaged);

        mockMvc.perform(get("/api/admin/inventory")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameId").value(populated.getId()))
                .andExpect(jsonPath("$[0].available").value(1))
                .andExpect(jsonPath("$[0].rented").value(1))
                .andExpect(jsonPath("$[0].lost").value(1))
                .andExpect(jsonPath("$[0].damaged").value(1))
                .andExpect(jsonPath("$[0].total").value(4))
                .andExpect(jsonPath("$[1].gameId").value(empty.getId()))
                .andExpect(jsonPath("$[1].total").value(0));
    }

    @Test
    void invalidRequestsAreRejectedAndMissingGameIsNotFound() throws Exception {
        mockMvc.perform(post("/api/admin/games")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": " ",
                                  "description": "",
                                  "rentalPrice": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.rentalPrice").exists());

        mockMvc.perform(post("/api/admin/games/999999/copies")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Game not found."));
    }

    @Test
    void clientAndAnonymousCannotManageCatalog() throws Exception {
        mockMvc.perform(get("/api/admin/inventory")
                        .header("Authorization", bearer(client)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/inventory"))
                .andExpect(status().isUnauthorized());
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
