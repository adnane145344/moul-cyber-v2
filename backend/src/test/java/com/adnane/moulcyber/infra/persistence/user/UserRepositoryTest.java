package com.adnane.moulcyber.infra.persistence.user;

import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.support.PostgreSQLContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest extends PostgreSQLContainerTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void canSaveAndFindUserByEmail() {
        User savedUser = userRepository.saveAndFlush(customer("customer@example.com"));

        assertThat(savedUser.getId()).isNotNull();
        assertThat(userRepository.findByEmail("customer@example.com"))
                .hasValueSatisfying(user -> {
                    assertThat(user.getFirstName()).isEqualTo("Adnane");
                    assertThat(user.getLastName()).isEqualTo("Lardi");
                    assertThat(user.getRole()).isEqualTo(Role.CLIENT);
                });
        assertThat(userRepository.existsByEmail("customer@example.com")).isTrue();
    }

    @Test
    void emailMustBeUnique() {
        userRepository.saveAndFlush(customer("unique@example.com"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(customer("unique@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User customer(String email) {
        return new User("Adnane", "Lardi", email, "password-hash", Role.CLIENT);
    }
}
