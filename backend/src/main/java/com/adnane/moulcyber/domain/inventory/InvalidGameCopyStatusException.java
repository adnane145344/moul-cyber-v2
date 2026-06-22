package com.adnane.moulcyber.domain.inventory;

public class InvalidGameCopyStatusException extends IllegalStateException {

    public InvalidGameCopyStatusException(String message) {
        super(message);
    }
}
