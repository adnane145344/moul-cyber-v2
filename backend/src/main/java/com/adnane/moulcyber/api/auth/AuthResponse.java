package com.adnane.moulcyber.api.auth;

import com.adnane.moulcyber.domain.user.Role;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        Role role) {
}
