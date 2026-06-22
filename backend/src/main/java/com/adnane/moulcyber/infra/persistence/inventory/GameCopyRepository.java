package com.adnane.moulcyber.infra.persistence.inventory;

import java.util.Collection;
import java.util.List;

import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameCopyRepository extends JpaRepository<GameCopy, Long> {

    List<GameCopy> findByGameIdAndStatus(Long gameId, GameCopyStatus status);

    long countByGameIdAndStatus(Long gameId, GameCopyStatus status);

    @Query("""
            select copy.game.id as gameId, count(copy.id) as availableCopies
            from GameCopy copy
            where copy.game.id in :gameIds and copy.status = :status
            group by copy.game.id
            """)
    List<GameAvailabilityCount> countAvailabilityByGameIds(
            @Param("gameIds") Collection<Long> gameIds,
            @Param("status") GameCopyStatus status);
}
