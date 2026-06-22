package com.adnane.moulcyber.api.catalog;

import java.util.List;

import com.adnane.moulcyber.application.catalog.CatalogService;
import com.adnane.moulcyber.application.catalog.GameDetailsResponse;
import com.adnane.moulcyber.application.catalog.GameSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameCatalogController {

    private final CatalogService catalogService;

    public GameCatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<GameSummaryResponse> findGames(
            @RequestParam(required = false) String title) {
        return catalogService.findGames(title);
    }

    @GetMapping("/{gameId}")
    public GameDetailsResponse findGame(@PathVariable Long gameId) {
        return catalogService.findGame(gameId);
    }
}
