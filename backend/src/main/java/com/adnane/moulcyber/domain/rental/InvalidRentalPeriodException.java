package com.adnane.moulcyber.domain.rental;

public class InvalidRentalPeriodException extends IllegalArgumentException {

    public InvalidRentalPeriodException(String message) {
        super(message);
    }
}
