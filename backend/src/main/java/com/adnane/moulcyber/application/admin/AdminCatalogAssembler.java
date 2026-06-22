package com.adnane.moulcyber.application.admin;

import com.adnane.moulcyber.domain.catalog.Game;
import org.springframework.stereotype.Component;

@Component
public class AdminCatalogAssembler {

    public AdminGameResponse toResponse(Game game) {
        return new AdminGameResponse(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getRentalPrice());
    }
}
