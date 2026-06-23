package com.adnane.moulcyber.application.auth;

import java.util.Optional;

import com.adnane.moulcyber.configuration.security.JwtService;
import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerCreatesClientUser() {
        RegisterRequest request = new RegisterRequest(
                "Adnane", "Lardi", " ADNANE@example.com ", "StrongPassword1!");
        when(userRepository.existsByEmail("adnane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPassword1!")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new User(42L, user.getFirstName(), user.getLastName(), user.getEmail(),
                    user.getPasswordHash(), user.getRole());
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("signed-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("adnane@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.CLIENT);
        assertThat(response).isEqualTo(new AuthResponse(
                "signed-token", 42L, "adnane@example.com", Role.CLIENT));
    }

    @Test
    void registerFailsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "Adnane", "Lardi", "adnane@example.com", "StrongPassword1!");
        when(userRepository.existsByEmail("adnane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).saveAndFlush(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registerConvertsConcurrentEmailCollisionToBusinessError() {
        RegisterRequest request = new RegisterRequest(
                "Adnane", "Lardi", "adnane@example.com", "StrongPassword1!");
        when(userRepository.existsByEmail("adnane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPassword1!")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreValid() {
        User user = new User(
                7L, "Adnane", "Lardi", "adnane@example.com", "encoded-password", Role.CLIENT);
        when(userRepository.findByEmail("adnane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("StrongPassword1!", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("signed-token");

        AuthResponse response = authService.login(
                new LoginRequest(" ADNANE@example.com ", "StrongPassword1!"));

        assertThat(response).isEqualTo(new AuthResponse(
                "signed-token", 7L, "adnane@example.com", Role.CLIENT));
    }

    @Test
    void loginFailsWhenPasswordIsInvalid() {
        User user = new User(
                7L, "Adnane", "Lardi", "adnane@example.com", "encoded-password", Role.CLIENT);
        when(userRepository.findByEmail("adnane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("adnane@example.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }
}
