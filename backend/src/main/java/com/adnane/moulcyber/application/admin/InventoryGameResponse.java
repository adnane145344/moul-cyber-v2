package com.adnane.moulcyber.application.admin;

public record InventoryGameResponse(
        Long gameId,
        String title,
        long available,
        long rented,
        long lost,
        long damaged,
        long total) {
}
