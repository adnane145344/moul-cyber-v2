package com.adnane.moulcyber.configuration.security;

import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.support.PostgreSQLContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityAccessTest.TestEndpoints.class)
class SecurityAccessTest extends PostgreSQLContainerTest {

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
    void publicCanAccessOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Moul Cyber API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.tags[?(@.name == 'Catalog')]").exists())
                .andExpect(jsonPath("$.tags[?(@.name == 'Admin Rentals')]").exists());

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void browserPreflightFromConfiguredFrontendOriginIsAllowed() throws Exception {
        mockMvc.perform(options("/api/games")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type"));
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
