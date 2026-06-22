package com.adnane.moulcyber.api.user;

import com.adnane.moulcyber.application.user.CurrentUserService;
import com.adnane.moulcyber.configuration.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class CurrentUserController {

    private final CurrentUserService currentUserService;

    public CurrentUserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return currentUserService.getCurrentUser(principal.userId());
    }
}
