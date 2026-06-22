package com.adnane.moulcyber.rental;

import java.util.Objects;

import com.adnane.moulcyber.inventory.GameCopy;

public class RentalItem {

    private final Long id;
    private final GameCopy gameCopy;

    public RentalItem(GameCopy gameCopy) {
        this(null, gameCopy);
    }

    public RentalItem(Long id, GameCopy gameCopy) {
        this.id = id;
        this.gameCopy = Objects.requireNonNull(gameCopy, "Game copy is required.");
    }

    public Long getId() {
        return id;
    }

    public GameCopy getGameCopy() {
        return gameCopy;
    }
}
