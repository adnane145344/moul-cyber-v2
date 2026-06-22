package com.adnane.moulcyber.infra.persistence.user;

import java.util.Optional;

import com.adnane.moulcyber.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
