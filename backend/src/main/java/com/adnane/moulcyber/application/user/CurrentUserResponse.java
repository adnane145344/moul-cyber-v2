package com.adnane.moulcyber.application.user;

import com.adnane.moulcyber.domain.user.Role;

public record CurrentUserResponse(
        Long userId,
        String firstName,
        String lastName,
        String email,
        Role role) {
}
