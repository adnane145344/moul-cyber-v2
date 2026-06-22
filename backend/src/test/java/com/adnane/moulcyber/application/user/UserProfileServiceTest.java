package com.adnane.moulcyber.application.user;

import java.util.Optional;

import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserProfileService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new UserProfileService(userRepository, passwordEncoder);
        user = new User(
                1L,
                "Old",
                "Name",
                "client@example.com",
                "old-hash",
                Role.CLIENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @Test
    void updatesTrimmedNamesWithoutChangingIdentityFields() {
        CurrentUserResponse response = service.updateProfile(
                1L, new UpdateProfileRequest("  New  ", "  Name  "));

        assertThat(response.firstName()).isEqualTo("New");
        assertThat(response.lastName()).isEqualTo("Name");
        assertThat(response.email()).isEqualTo("client@example.com");
        assertThat(response.role()).isEqualTo(Role.CLIENT);
    }

    @Test
    void changesPasswordAfterVerifyingCurrentPassword() {
        when(passwordEncoder.matches("CurrentPassword1!", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword1!", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("new-hash");

        service.changePassword(
                1L,
                new ChangePasswordRequest("CurrentPassword1!", "NewPassword1!"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(passwordEncoder).encode("NewPassword1!");
    }

    @Test
    void rejectsIncorrectCurrentPasswordAndPasswordReuse() {
        when(passwordEncoder.matches("WrongPassword1!", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(
                1L,
                new ChangePasswordRequest("WrongPassword1!", "NewPassword1!")))
                .isInstanceOf(InvalidCurrentPasswordException.class);

        when(passwordEncoder.matches("CurrentPassword1!", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("SamePassword1!", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(
                1L,
                new ChangePasswordRequest("CurrentPassword1!", "SamePassword1!")))
                .isInstanceOf(PasswordReuseException.class);
    }
}
