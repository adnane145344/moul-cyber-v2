package com.adnane.moulcyber.application.rental;

public class GameUnavailableException extends RuntimeException {

    public GameUnavailableException() {
        super("No copy is currently available for this game.");
    }
}
