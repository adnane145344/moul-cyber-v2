package com.adnane.moulcyber.domain.review;

public class InvalidReviewException extends IllegalArgumentException {

    public InvalidReviewException(String message) {
        super(message);
    }
}
