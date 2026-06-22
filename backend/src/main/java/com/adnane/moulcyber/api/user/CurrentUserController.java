package com.adnane.moulcyber.api.user;

import com.adnane.moulcyber.application.user.CurrentUserService;
import com.adnane.moulcyber.application.user.CurrentUserResponse;
import com.adnane.moulcyber.application.user.ChangePasswordRequest;
import com.adnane.moulcyber.application.user.UpdateProfileRequest;
import com.adnane.moulcyber.application.user.UserProfileService;
import com.adnane.moulcyber.configuration.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class CurrentUserController {

    private final CurrentUserService currentUserService;
    private final UserProfileService userProfileService;

    public CurrentUserController(
            CurrentUserService currentUserService,
            UserProfileService userProfileService) {
        this.currentUserService = currentUserService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return currentUserService.getCurrentUser(principal.userId());
    }

    @PutMapping("/me")
    public CurrentUserResponse updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return userProfileService.updateProfile(principal.userId(), request);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(principal.userId(), request);
    }
}
