package com.adnane.moulcyber.api.admin;

import java.util.List;

import com.adnane.moulcyber.application.admin.AdminRentalDetailsResponse;
import com.adnane.moulcyber.application.admin.AdminRentalFilter;
import com.adnane.moulcyber.application.admin.AdminRentalService;
import com.adnane.moulcyber.application.admin.AdminRentalSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rentals")
public class AdminRentalController {

    private final AdminRentalService rentalService;

    public AdminRentalController(AdminRentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<AdminRentalSummaryResponse> rentals(
            @RequestParam(required = false) AdminRentalFilter status) {
        return rentalService.findRentals(status);
    }

    @GetMapping("/{rentalId}")
    public AdminRentalDetailsResponse rental(@PathVariable Long rentalId) {
        return rentalService.findRental(rentalId);
    }
}
