package com.adnane.moulcyber.domain.rental;

public class InvalidRentalItemStatusException extends IllegalStateException {

    public InvalidRentalItemStatusException(String message) {
        super(message);
    }
}
