package com.adnane.moulcyber.application.user;

public class InvalidCurrentPasswordException extends RuntimeException {

    public InvalidCurrentPasswordException() {
        super("Current password is incorrect.");
    }
}
