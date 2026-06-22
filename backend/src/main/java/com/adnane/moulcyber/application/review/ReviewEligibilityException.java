package com.adnane.moulcyber.application.review;

public class ReviewEligibilityException extends RuntimeException {

    public ReviewEligibilityException() {
        super("A completed rental is required before reviewing this game.");
    }
}
