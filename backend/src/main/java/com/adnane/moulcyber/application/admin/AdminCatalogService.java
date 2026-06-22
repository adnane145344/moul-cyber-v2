package com.adnane.moulcyber.application.admin;

import java.util.ArrayList;
import java.util.List;

import com.adnane.moulcyber.application.catalog.GameNotFoundException;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCatalogService {

    private final GameRepository gameRepository;
    private final GameCopyRepository gameCopyRepository;
    private final AdminCatalogAssembler assembler;

    public AdminCatalogService(
            GameRepository gameRepository,
            GameCopyRepository gameCopyRepository,
            AdminCatalogAssembler assembler) {
        this.gameRepository = gameRepository;
        this.gameCopyRepository = gameCopyRepository;
        this.assembler = assembler;
    }

    @Transactional
    public AdminGameResponse createGame(CreateGameRequest request) {
        Game game = new Game(request.title(), request.description(), request.rentalPrice());
        return assembler.toResponse(gameRepository.save(game));
    }

    @Transactional
    public AdminGameResponse updateGame(Long gameId, UpdateGameRequest request) {
        Game game = findGame(gameId);
        game.updateDetails(request.title(), request.description(), request.rentalPrice());
        return assembler.toResponse(game);
    }

    @Transactional
    public AddGameCopiesResponse addCopies(Long gameId, AddGameCopiesRequest request) {
        Game game = findGame(gameId);
        List<GameCopy> copies = new ArrayList<>(request.quantity());
        for (int index = 0; index < request.quantity(); index++) {
            copies.add(new GameCopy(game));
        }
        gameCopyRepository.saveAll(copies);
        long totalCopies = gameCopyRepository.countByGameId(gameId);
        return new AddGameCopiesResponse(gameId, request.quantity(), totalCopies);
    }

    private Game findGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(GameNotFoundException::new);
    }
}
