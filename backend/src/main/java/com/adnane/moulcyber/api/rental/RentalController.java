package com.adnane.moulcyber.api.rental;

import java.util.List;

import com.adnane.moulcyber.application.rental.CreateRentalRequest;
import com.adnane.moulcyber.application.rental.RentalResponse;
import com.adnane.moulcyber.application.rental.RentalService;
import com.adnane.moulcyber.application.rental.RentalSummaryResponse;
import com.adnane.moulcyber.configuration.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse createRental(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateRentalRequest request) {
        return rentalService.createRental(principal.userId(), request);
    }

    @GetMapping("/me")
    public List<RentalSummaryResponse> currentUserRentals(
            @AuthenticationPrincipal UserPrincipal principal) {
        return rentalService.findUserRentals(principal.userId());
    }

    @GetMapping("/{rentalId}")
    public RentalResponse rentalDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long rentalId) {
        return rentalService.findUserRental(principal.userId(), rentalId);
    }
}
