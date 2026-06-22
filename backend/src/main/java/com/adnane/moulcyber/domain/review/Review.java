package com.adnane.moulcyber.domain.review;

import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reviews_user_game",
                columnNames = {"user_id", "game_id"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private int rating;

    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String comment;

    protected Review() {
    }

    public Review(User user, Game game, int rating, String comment) {
        this(null, user, game, rating, comment);
    }

    public Review(Long id, User user, Game game, int rating, String comment) {
        this.id = id;
        this.user = requireUser(user);
        this.game = requireGame(game);
        this.rating = requireValidRating(rating);
        this.comment = requireComment(comment);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Game getGame() {
        return game;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    private static User requireUser(User user) {
        if (user == null) {
            throw new InvalidReviewException("Review user is required.");
        }
        return user;
    }

    private static Game requireGame(Game game) {
        if (game == null) {
            throw new InvalidReviewException("Reviewed game is required.");
        }
        return game;
    }

    private static int requireValidRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new InvalidReviewException("Rating must be between 1 and 5.");
        }
        return rating;
    }

    private static String requireComment(String comment) {
        if (comment == null || comment.isBlank()) {
            throw new InvalidReviewException("Review comment cannot be blank.");
        }
        return comment.trim();
    }
}
