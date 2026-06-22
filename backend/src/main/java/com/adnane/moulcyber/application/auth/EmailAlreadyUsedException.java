package com.adnane.moulcyber.application.auth;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException() {
        super("Email is already in use.");
    }
}
