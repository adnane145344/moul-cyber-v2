package com.adnane.moulcyber.application.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("Authenticated user no longer exists.");
    }
}
