package com.adnane.moulcyber.configuration.security;

import com.adnane.moulcyber.domain.user.Role;

public record UserPrincipal(
        Long userId,
        String email,
        Role role) {
}
