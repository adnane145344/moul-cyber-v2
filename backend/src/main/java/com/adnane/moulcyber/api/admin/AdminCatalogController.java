package com.adnane.moulcyber.api.admin;

import java.util.List;

import com.adnane.moulcyber.application.admin.AddGameCopiesRequest;
import com.adnane.moulcyber.application.admin.AddGameCopiesResponse;
import com.adnane.moulcyber.application.admin.AdminCatalogService;
import com.adnane.moulcyber.application.admin.AdminGameResponse;
import com.adnane.moulcyber.application.admin.AdminInventoryService;
import com.adnane.moulcyber.application.admin.CreateGameRequest;
import com.adnane.moulcyber.application.admin.InventoryGameResponse;
import com.adnane.moulcyber.application.admin.UpdateGameRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Admin Catalog", description = "Administrative catalog and inventory management endpoints.")
@RequestMapping("/api/admin")
public class AdminCatalogController {

    private final AdminCatalogService catalogService;
    private final AdminInventoryService inventoryService;

    public AdminCatalogController(
            AdminCatalogService catalogService,
            AdminInventoryService inventoryService) {
        this.catalogService = catalogService;
        this.inventoryService = inventoryService;
    }

    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminGameResponse createGame(@Valid @RequestBody CreateGameRequest request) {
        return catalogService.createGame(request);
    }

    @PutMapping("/games/{gameId}")
    public AdminGameResponse updateGame(
            @PathVariable Long gameId,
            @Valid @RequestBody UpdateGameRequest request) {
        return catalogService.updateGame(gameId, request);
    }

    @PostMapping("/games/{gameId}/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public AddGameCopiesResponse addCopies(
            @PathVariable Long gameId,
            @Valid @RequestBody AddGameCopiesRequest request) {
        return catalogService.addCopies(gameId, request);
    }

    @GetMapping("/inventory")
    public List<InventoryGameResponse> inventory() {
        return inventoryService.findInventory();
    }
}
