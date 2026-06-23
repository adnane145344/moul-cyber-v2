package com.adnane.moulcyber.configuration.security;

import java.time.Duration;

import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String VALID_SECRET =
            "dGVzdC1qd3Qtc2VjcmV0LWZvci1tb3VsLWN5YmVyLW11c3QtYmUtMzItYnl0ZXMtbG9uZw==";

    @Test
    void generatesAndParsesToken() {
        JwtService jwtService = new JwtService(VALID_SECRET, Duration.ofHours(1));
        User user = new User(42L, "Adnane", "Lardi", "adnane@example.com",
                "password-hash", Role.ADMIN);

        UserPrincipal principal = jwtService.parseToken(jwtService.generateToken(user));

        assertThat(principal.userId()).isEqualTo(42L);
        assertThat(principal.email()).isEqualTo("adnane@example.com");
        assertThat(principal.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void rejectsMissingSecret() {
        assertThatThrownBy(() -> new JwtService("", Duration.ofHours(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must be configured.");
    }

    @Test
    void rejectsInvalidBase64Secret() {
        assertThatThrownBy(() -> new JwtService("not-base64", Duration.ofHours(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must be valid Base64.");
    }

    @Test
    void rejectsWeakSecret() {
        assertThatThrownBy(() -> new JwtService("d2Vhaw==", Duration.ofHours(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must be at least 32 bytes.");
    }

    @Test
    void rejectsNonPositiveExpiration() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, Duration.ZERO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT expiration must be positive.");
    }
}
