package com.adnane.moulcyber.api.user;

import com.adnane.moulcyber.domain.user.Role;

public record CurrentUserResponse(
        Long userId,
        String firstName,
        String lastName,
        String email,
        Role role) {
}
