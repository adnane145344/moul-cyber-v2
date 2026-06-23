package com.adnane.moulcyber.api.admin;

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
import com.adnane.moulcyber.support.PostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminRentalQueryApiTest extends PostgreSQLContainerTest {

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

    private User admin;
    private User client;
    private Game game;

    @BeforeEach
    void setUp() {
        admin = userRepository.saveAndFlush(new User(
                "Admin", "User", "admin.query@example.com", "hash", Role.ADMIN));
        client = userRepository.saveAndFlush(new User(
                "Client", "Customer", "client.query@example.com", "hash", Role.CLIENT));
        game = gameRepository.saveAndFlush(new Game(
                "Cyber Quest", "Adventure.", new BigDecimal("5.00")));
    }

    @Test
    void adminCanListFilterAndReadRentalDetails() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Rental active = saveRental(today, today.plusDays(7), false);
        Rental overdue = saveRental(today.minusDays(10), today.minusDays(3), false);
        Rental completed = saveRental(today.minusDays(9), today.minusDays(2), true);

        mockMvc.perform(get("/api/admin/rentals")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(get("/api/admin/rentals")
                        .param("status", "ACTIVE")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(active.getId()))
                .andExpect(jsonPath("$.content[0].overdue").value(false));

        mockMvc.perform(get("/api/admin/rentals")
                        .param("status", "OVERDUE")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(overdue.getId()))
                .andExpect(jsonPath("$.content[0].overdue").value(true));

        mockMvc.perform(get("/api/admin/rentals")
                        .param("status", "COMPLETED")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(completed.getId()))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));

        mockMvc.perform(get("/api/admin/rentals/{rentalId}", active.getId())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(client.getId()))
                .andExpect(jsonPath("$.customerName").value("Client Customer"))
                .andExpect(jsonPath("$.customerEmail").value("client.query@example.com"))
                .andExpect(jsonPath("$.items[0].gameTitle").value("Cyber Quest"));
    }

    @Test
    void adminCanPageRentals() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Rental first = saveRental(today, today.plusDays(7), false);
        Rental second = saveRental(today.minusDays(1), today.plusDays(6), false);
        saveRental(today.minusDays(2), today.plusDays(5), false);

        mockMvc.perform(get("/api/admin/rentals")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(first.getId()))
                .andExpect(jsonPath("$.content[1].id").value(second.getId()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/admin/rentals")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void invalidFilterAndMissingRentalReturnApiErrors() throws Exception {
        mockMvc.perform(get("/api/admin/rentals")
                        .param("status", "UNKNOWN")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request parameter."));

        mockMvc.perform(get("/api/admin/rentals")
                        .param("size", "101")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed."));

        mockMvc.perform(get("/api/admin/rentals/999999")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Rental not found."));
    }

    private Rental saveRental(LocalDate start, LocalDate due, boolean completed) {
        GameCopy copy = new GameCopy(game);
        copy.rent();
        gameCopyRepository.save(copy);
        Rental rental = new Rental(client, start, due);
        RentalItem item = new RentalItem(copy, game.getRentalPrice());
        rental.addItem(item);
        if (completed) {
            item.returnOn(due);
        }
        return rentalRepository.saveAndFlush(rental);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
