package com.adnane.moulcyber.application.review;

public class DuplicateReviewException extends RuntimeException {

    public DuplicateReviewException() {
        super("You have already reviewed this game.");
    }
}
