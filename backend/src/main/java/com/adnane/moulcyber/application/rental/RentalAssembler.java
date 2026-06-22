package com.adnane.moulcyber.application.rental;

import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItem;
import org.springframework.stereotype.Component;

@Component
public class RentalAssembler {

    public RentalResponse toResponse(Rental rental) {
        return new RentalResponse(
                rental.getId(),
                rental.getStatus(),
                rental.getStartDate(),
                rental.getDueDate(),
                rental.getItems().stream().map(this::toItemResponse).toList());
    }

    public RentalSummaryResponse toSummary(Rental rental) {
        return new RentalSummaryResponse(
                rental.getId(),
                rental.getStatus(),
                rental.getStartDate(),
                rental.getDueDate(),
                rental.getItems().size());
    }

    public RentalItemResponse toItemResponse(RentalItem item) {
        return new RentalItemResponse(
                item.getId(),
                item.getGameCopy().getGame().getId(),
                item.getGameCopy().getGame().getTitle(),
                item.getGameCopy().getId(),
                item.getStatus(),
                item.getRentalPrice(),
                item.getProcessedDate(),
                item.getLateFee());
    }
}
