package com.adnane.moulcyber.application.catalog;

import com.adnane.moulcyber.domain.catalog.Game;
import org.springframework.stereotype.Component;

@Component
public class GameCatalogAssembler {

    public GameSummaryResponse toSummary(Game game, long availableCopies) {
        return new GameSummaryResponse(
                game.getId(),
                game.getTitle(),
                game.getRentalPrice(),
                availableCopies);
    }

    public GameDetailsResponse toDetails(Game game, long availableCopies) {
        return new GameDetailsResponse(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getRentalPrice(),
                availableCopies);
    }
}
