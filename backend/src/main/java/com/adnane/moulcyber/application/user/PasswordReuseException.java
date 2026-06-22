package com.adnane.moulcyber.application.user;

public class PasswordReuseException extends RuntimeException {

    public PasswordReuseException() {
        super("New password must be different from current password.");
    }
}
