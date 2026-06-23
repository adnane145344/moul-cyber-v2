package com.adnane.moulcyber.api.rental;

import java.math.BigDecimal;

import com.adnane.moulcyber.configuration.security.JwtService;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import com.adnane.moulcyber.infra.persistence.rental.RentalRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RentalApiTest extends PostgreSQLContainerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameCopyRepository gameCopyRepository;
    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private JwtService jwtService;

    private User client;
    private Game game;

    @BeforeEach
    void setUp() {
        client = userRepository.saveAndFlush(new User(
                "Adnane", "Lardi", "rental.api@example.com", "password-hash", Role.CLIENT));
        game = gameRepository.saveAndFlush(new Game(
                "Cyber Quest", "Adventure.", new BigDecimal("5.00")));
    }

    @Test
    void anonymousCannotCreateRentalAndReceivesApiError() throws Exception {
        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":" + game.getId() + "}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void authenticatedClientCanCreateAndReadOwnRental() throws Exception {
        GameCopy copy = gameCopyRepository.saveAndFlush(new GameCopy(game));

        String response = mockMvc.perform(post("/api/rentals")
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":" + game.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].gameId").value(game.getId()))
                .andExpect(jsonPath("$.items[0].copyId").value(copy.getId()))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].rentalPrice").value(5.00))
                .andExpect(jsonPath("$.items[0].gameCopy").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        long rentalId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/rentals/me")
                        .header("Authorization", bearer(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(rentalId))
                .andExpect(jsonPath("$[0].itemCount").value(1));

        mockMvc.perform(get("/api/rentals/{id}", rentalId)
                        .header("Authorization", bearer(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rentalId))
                .andExpect(jsonPath("$.items[0].gameTitle").value("Cyber Quest"));

        assertThat(gameCopyRepository.findById(copy.getId()).orElseThrow().getStatus())
                .isEqualTo(GameCopyStatus.RENTED);
    }

    @Test
    void unavailableGameReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/rentals")
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":" + game.getId() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("No copy is currently available for this game."));
    }

    @Test
    void missingGameReturnsNotFoundAndInvalidRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/rentals")
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Game not found."));

        mockMvc.perform(post("/api/rentals")
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.gameId").exists());
    }

    @Test
    void clientCannotReadAnotherUsersRental() throws Exception {
        gameCopyRepository.saveAndFlush(new GameCopy(game));
        String response = mockMvc.perform(post("/api/rentals")
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameId\":" + game.getId() + "}"))
                .andReturn().getResponse().getContentAsString();
        long rentalId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();
        User other = userRepository.saveAndFlush(new User(
                "Other", "Client", "other@example.com", "password-hash", Role.CLIENT));

        mockMvc.perform(get("/api/rentals/{id}", rentalId)
                        .header("Authorization", bearer(other)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Rental not found."));
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
