package com.adnane.moulcyber.api.rental;

import com.adnane.moulcyber.application.rental.RentalItemProcessingResponse;
import com.adnane.moulcyber.application.rental.RentalItemProcessingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Admin Rental Items", description = "Administrative rental item processing endpoints.")
@RequestMapping("/api/admin/rental-items")
public class AdminRentalItemController {

    private final RentalItemProcessingService processingService;

    public AdminRentalItemController(RentalItemProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping("/{itemId}/return")
    public RentalItemProcessingResponse returnItem(@PathVariable Long itemId) {
        return processingService.returnItem(itemId);
    }

    @PostMapping("/{itemId}/mark-lost")
    public RentalItemProcessingResponse markLost(@PathVariable Long itemId) {
        return processingService.markLost(itemId);
    }

    @PostMapping("/{itemId}/mark-damaged")
    public RentalItemProcessingResponse markDamaged(@PathVariable Long itemId) {
        return processingService.markDamaged(itemId);
    }
}
