package com.adnane.moulcyber.domain.user;

import java.util.Objects;

public class User {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String passwordHash;
    private final Role role;

    public User(String firstName, String lastName, String email, String passwordHash, Role role) {
        this(null, firstName, lastName, email, passwordHash, role);
    }

    public User(Long id, String firstName, String lastName, String email, String passwordHash, Role role) {
        this.id = id;
        this.firstName = requireText(firstName, "First name");
        this.lastName = requireText(lastName, "Last name");
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

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}
