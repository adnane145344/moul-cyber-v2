package com.adnane.moulcyber.domain.inventory;

import java.util.Objects;

import com.adnane.moulcyber.domain.catalog.Game;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_copies")
public class GameCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameCopyStatus status;

    protected GameCopy() {
    }

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
        status = GameCopyStatus.AVAILABLE;
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
