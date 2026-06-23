package com.adnane.moulcyber.api.admin;

import com.adnane.moulcyber.application.admin.AdminRentalDetailsResponse;
import com.adnane.moulcyber.application.admin.AdminRentalFilter;
import com.adnane.moulcyber.application.admin.AdminRentalService;
import com.adnane.moulcyber.application.admin.AdminRentalSummaryResponse;
import com.adnane.moulcyber.application.shared.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/admin/rentals")
public class AdminRentalController {

    private final AdminRentalService rentalService;

    public AdminRentalController(AdminRentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public PageResponse<AdminRentalSummaryResponse> rentals(
            @RequestParam(required = false) AdminRentalFilter status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        return rentalService.findRentals(status, pageable);
    }

    @GetMapping("/{rentalId}")
    public AdminRentalDetailsResponse rental(@PathVariable Long rentalId) {
        return rentalService.findRental(rentalId);
    }
}
