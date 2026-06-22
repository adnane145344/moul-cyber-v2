package com.adnane.moulcyber.configuration.security;

import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityAccessTest.TestEndpoints.class)
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void publicCanAccessGamesEndpoint() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotAccessRentals() throws Exception {
        mockMvc.perform(get("/api/rentals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/ping")
                        .header("Authorization", bearerToken(Role.CLIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/ping")
                        .header("Authorization", bearerToken(Role.ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/rentals")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerToken(Role role) {
        User user = new User(
                99L, "Security", "Tester", role.name().toLowerCase() + "@example.com",
                "password-hash", role);
        return "Bearer " + jwtService.generateToken(user);
    }

    @RestController
    static class TestEndpoints {

        @GetMapping("/api/games")
        String games() {
            return "games";
        }

        @GetMapping("/api/rentals")
        String rentals() {
            return "rentals";
        }

        @GetMapping("/api/admin/ping")
        String admin() {
            return "admin";
        }
    }
}
