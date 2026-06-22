package com.adnane.moulcyber.application.admin;

import java.time.LocalDate;

import com.adnane.moulcyber.application.rental.RentalAssembler;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class AdminRentalAssembler {

    private final RentalAssembler rentalAssembler;

    public AdminRentalAssembler(RentalAssembler rentalAssembler) {
        this.rentalAssembler = rentalAssembler;
    }

    public AdminRentalSummaryResponse toSummary(Rental rental, LocalDate today) {
        User user = rental.getUser();
        return new AdminRentalSummaryResponse(
                rental.getId(),
                user.getId(),
                customerName(user),
                user.getEmail(),
                rental.getStatus(),
                rental.isOverdueOn(today),
                rental.getStartDate(),
                rental.getDueDate(),
                rental.getItems().size());
    }

    public AdminRentalDetailsResponse toDetails(Rental rental, LocalDate today) {
        User user = rental.getUser();
        return new AdminRentalDetailsResponse(
                rental.getId(),
                user.getId(),
                customerName(user),
                user.getEmail(),
                rental.getStatus(),
                rental.isOverdueOn(today),
                rental.getStartDate(),
                rental.getDueDate(),
                rental.getItems().stream()
                        .map(rentalAssembler::toItemResponse)
                        .toList());
    }

    private String customerName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
