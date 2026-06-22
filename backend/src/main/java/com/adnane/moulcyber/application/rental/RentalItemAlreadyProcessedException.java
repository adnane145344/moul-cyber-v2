package com.adnane.moulcyber.application.rental;

public class RentalItemAlreadyProcessedException extends RuntimeException {

    public RentalItemAlreadyProcessedException() {
        super("Rental item has already been processed.");
    }
}
