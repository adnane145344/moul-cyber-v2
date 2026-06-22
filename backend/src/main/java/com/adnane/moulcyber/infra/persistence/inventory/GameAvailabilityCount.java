package com.adnane.moulcyber.infra.persistence.inventory;

public interface GameAvailabilityCount {

    Long getGameId();

    long getAvailableCopies();
}
