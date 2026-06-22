package com.adnane.moulcyber.domain.rental;

import java.util.Objects;

import com.adnane.moulcyber.domain.inventory.GameCopy;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rental_items")
public class RentalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_copy_id", nullable = false)
    private GameCopy gameCopy;

    protected RentalItem() {
    }

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

    public Rental getRental() {
        return rental;
    }

    void attachTo(Rental rental) {
        this.rental = Objects.requireNonNull(rental, "Rental is required.");
    }
}
