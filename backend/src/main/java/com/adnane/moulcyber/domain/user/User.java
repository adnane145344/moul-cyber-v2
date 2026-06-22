package com.adnane.moulcyber.domain.user;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    protected User() {
    }

    public User(String firstName, String lastName, String email, String passwordHash, Role role) {
        this(null, firstName, lastName, email, passwordHash, role);
    }

    public User(Long id, String firstName, String lastName, String email, String passwordHash, Role role) {
        this.id = id;
        this.firstName = requireName(firstName, "First name");
        this.lastName = requireName(lastName, "Last name");
        this.email = requireText(email, "Email");
        this.passwordHash = requireText(passwordHash, "Password hash");
        this.role = Objects.requireNonNull(role, "Role is required.");
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void updateProfile(String firstName, String lastName) {
        this.firstName = requireName(firstName, "First name");
        this.lastName = requireName(lastName, "Last name");
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = requireText(passwordHash, "Password hash");
    }

    private static String requireName(String value, String fieldName) {
        String name = requireText(value, fieldName);
        if (name.length() > 100) {
            throw new IllegalArgumentException(fieldName + " must not exceed 100 characters.");
        }
        return name;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}
