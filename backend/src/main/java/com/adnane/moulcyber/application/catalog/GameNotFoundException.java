package com.adnane.moulcyber.application.catalog;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException() {
        super("Game not found.");
    }
}
