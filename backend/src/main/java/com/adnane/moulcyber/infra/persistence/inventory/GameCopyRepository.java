package com.adnane.moulcyber.infra.persistence.inventory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameCopyRepository extends JpaRepository<GameCopy, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GameCopy> findFirstByGameIdAndStatusOrderByIdAsc(
            Long gameId,
            GameCopyStatus status);

    List<GameCopy> findByGameIdAndStatus(Long gameId, GameCopyStatus status);

    long countByGameIdAndStatus(Long gameId, GameCopyStatus status);

    long countByGameId(Long gameId);

    @Query("""
            select copy.game.id as gameId, count(copy.id) as availableCopies
            from GameCopy copy
            where copy.game.id in :gameIds and copy.status = :status
            group by copy.game.id
            """)
    List<GameAvailabilityCount> countAvailabilityByGameIds(
            @Param("gameIds") Collection<Long> gameIds,
            @Param("status") GameCopyStatus status);

    @Query("""
            select copy.game.id as gameId, copy.status as status, count(copy.id) as copyCount
            from GameCopy copy
            group by copy.game.id, copy.status
            """)
    List<GameCopyStatusCount> countCopiesGroupedByGameAndStatus();
}
