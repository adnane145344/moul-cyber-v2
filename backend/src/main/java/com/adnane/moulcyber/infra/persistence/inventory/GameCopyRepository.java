package com.adnane.moulcyber.infra.persistence.inventory;

import java.util.List;

import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameCopyRepository extends JpaRepository<GameCopy, Long> {

    List<GameCopy> findByGameIdAndStatus(Long gameId, GameCopyStatus status);

    long countByGameIdAndStatus(Long gameId, GameCopyStatus status);
}
