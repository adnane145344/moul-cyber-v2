package com.adnane.moulcyber.application.auth;

import com.adnane.moulcyber.domain.user.Role;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        Role role) {
}
