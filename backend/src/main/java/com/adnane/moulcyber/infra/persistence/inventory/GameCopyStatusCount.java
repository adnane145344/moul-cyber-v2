package com.adnane.moulcyber.infra.persistence.inventory;

import com.adnane.moulcyber.domain.inventory.GameCopyStatus;

public interface GameCopyStatusCount {

    Long getGameId();

    GameCopyStatus getStatus();

    long getCopyCount();
}
