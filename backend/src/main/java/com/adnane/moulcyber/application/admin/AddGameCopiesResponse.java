package com.adnane.moulcyber.application.admin;

public record AddGameCopiesResponse(
        Long gameId,
        int addedCopies,
        long totalCopies) {
}
