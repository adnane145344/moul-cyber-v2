package com.adnane.moulcyber.inventory;

import java.util.Objects;

import com.adnane.moulcyber.catalog.Game;

public class GameCopy {

    private final Long id;
    private final Game game;
    private GameCopyStatus status;

    public GameCopy(Game game) {
        this(null, game, GameCopyStatus.AVAILABLE);
    }

    public GameCopy(Long id, Game game, GameCopyStatus status) {
        this.id = id;
        this.game = Objects.requireNonNull(game, "Game is required.");
        this.status = Objects.requireNonNull(status, "Game copy status is required.");
    }

    public void rent() {
        requireStatus(GameCopyStatus.AVAILABLE, "rent");
        status = GameCopyStatus.RENTED;
    }

    public void markAsReturned() {
        requireStatus(GameCopyStatus.RENTED, "return");
        status = GameCopyStatus.RETURNED;
    }

    public void markAsLost() {
        status = GameCopyStatus.LOST;
    }

    public void markAsDamaged() {
        status = GameCopyStatus.DAMAGED;
    }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public GameCopyStatus getStatus() {
        return status;
    }

    private void requireStatus(GameCopyStatus expectedStatus, String operation) {
        if (status != expectedStatus) {
            throw new InvalidGameCopyStatusException(
                    "Cannot " + operation + " a game copy with status " + status + ".");
        }
    }
}
