package com.adnane.moulcyber.application.rental;

public class RentalItemNotFoundException extends RuntimeException {

    public RentalItemNotFoundException() {
        super("Rental item not found.");
    }
}
