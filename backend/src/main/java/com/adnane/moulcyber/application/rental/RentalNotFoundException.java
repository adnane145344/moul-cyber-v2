package com.adnane.moulcyber.application.rental;

public class RentalNotFoundException extends RuntimeException {

    public RentalNotFoundException() {
        super("Rental not found.");
    }
}
