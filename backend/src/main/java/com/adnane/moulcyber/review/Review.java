package com.adnane.moulcyber.review;

import com.adnane.moulcyber.catalog.Game;
import com.adnane.moulcyber.user.User;

public class Review {

    private final Long id;
    private final User user;
    private final Game game;
    private final int rating;
    private final String comment;

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
