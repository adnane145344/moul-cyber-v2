package com.adnane.moulcyber.api.rental;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminRentalItemApiTest {

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
    private User admin;
    private Game game;

    @BeforeEach
    void setUp() {
        client = userRepository.saveAndFlush(new User(
                "Client", "User", "processing.client@example.com",
                "password-hash", Role.CLIENT));
        admin = userRepository.saveAndFlush(new User(
                "Admin", "User", "processing.admin@example.com",
                "password-hash", Role.ADMIN));
        game = gameRepository.saveAndFlush(new Game(
                "Cyber Quest", "Adventure.", new BigDecimal("5.00")));
    }

    @Test
    void adminCanReturnLateItemAndSecondProcessingConflicts() throws Exception {
        RentalItem item = savedActiveItem(LocalDate.now(ZoneOffset.UTC).minusDays(3));

        mockMvc.perform(post("/api/admin/rental-items/{id}/return", item.getId())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LATE_RETURNED"))
                .andExpect(jsonPath("$.copyStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.lateFee").value(6.00));

        mockMvc.perform(post("/api/admin/rental-items/{id}/return", item.getId())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Rental item has already been processed."));
    }

    @Test
    void adminCanMarkItemsLostAndDamaged() throws Exception {
        RentalItem lost = savedActiveItem(LocalDate.now(ZoneOffset.UTC).plusDays(7));
        RentalItem damaged = savedActiveItem(LocalDate.now(ZoneOffset.UTC).plusDays(7));

        mockMvc.perform(post("/api/admin/rental-items/{id}/mark-lost", lost.getId())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOST"))
                .andExpect(jsonPath("$.copyStatus").value("LOST"));

        mockMvc.perform(post("/api/admin/rental-items/{id}/mark-damaged", damaged.getId())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DAMAGED"))
                .andExpect(jsonPath("$.copyStatus").value("DAMAGED"));
    }

    @Test
    void clientIsForbiddenAnonymousIsUnauthorizedAndMissingItemIsNotFound() throws Exception {
        RentalItem item = savedActiveItem(LocalDate.now(ZoneOffset.UTC).plusDays(7));

        mockMvc.perform(post("/api/admin/rental-items/{id}/return", item.getId())
                        .header("Authorization", bearer(client)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(post("/api/admin/rental-items/{id}/return", item.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/admin/rental-items/999999/return")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Rental item not found."));
    }

    private RentalItem savedActiveItem(LocalDate dueDate) {
        GameCopy copy = gameCopyRepository.saveAndFlush(new GameCopy(game));
        copy.rent();
        Rental rental = new Rental(
                client,
                dueDate.minusDays(7),
                dueDate);
        RentalItem item = new RentalItem(copy, game.getRentalPrice());
        rental.addItem(item);
        rentalRepository.saveAndFlush(rental);
        return item;
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
