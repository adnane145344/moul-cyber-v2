package com.adnane.moulcyber.api.review;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.adnane.moulcyber.configuration.security.JwtService;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItem;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import com.adnane.moulcyber.infra.persistence.rental.RentalRepository;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewApiTest {

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
                "Adnane", "Lardi", "review.api@example.com",
                "password-hash", Role.CLIENT));
        game = gameRepository.saveAndFlush(new Game(
                "Cyber Quest", "Adventure.", new BigDecimal("5.00")));
    }

    @Test
    void eligibleClientCanCreateReviewAndAnonymousCanListIt() throws Exception {
        completeRental();

        mockMvc.perform(post("/api/games/{id}/reviews", game.getId())
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Excellent cooperative game."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.authorName").value("Adnane"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.game").doesNotExist());

        mockMvc.perform(get("/api/games/{id}/reviews", game.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment")
                        .value("Excellent cooperative game."));
    }

    @Test
    void anonymousCannotCreateReviewAndNonEligibleClientIsForbidden() throws Exception {
        String body = "{\"rating\":5,\"comment\":\"Excellent.\"}";

        mockMvc.perform(post("/api/games/{id}/reviews", game.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/games/{id}/reviews", game.getId())
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("A completed rental is required before reviewing this game."));
    }

    @Test
    void duplicateAndInvalidReviewsAreRejected() throws Exception {
        completeRental();
        String body = "{\"rating\":5,\"comment\":\"Excellent.\"}";
        mockMvc.perform(post("/api/games/{id}/reviews", game.getId())
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/games/{id}/reviews", game.getId())
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("You have already reviewed this game."));

        mockMvc.perform(post("/api/games/{id}/reviews", game.getId())
                        .header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":6,\"comment\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating").exists())
                .andExpect(jsonPath("$.errors.comment").exists());
    }

    @Test
    void missingGameReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/games/999999/reviews"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Game not found."));
    }

    private void completeRental() {
        GameCopy copy = gameCopyRepository.saveAndFlush(new GameCopy(game));
        copy.rent();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = today.minusDays(10);
        Rental rental = new Rental(client, startDate, startDate.plusDays(7));
        RentalItem item = new RentalItem(copy, game.getRentalPrice());
        rental.addItem(item);
        item.returnOn(today);
        rentalRepository.saveAndFlush(rental);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
