package com.adnane.moulcyber.infra.persistence.rental;

import java.util.Optional;

import com.adnane.moulcyber.domain.rental.RentalItem;
import com.adnane.moulcyber.domain.rental.RentalItemStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select item
            from RentalItem item
            join fetch item.rental rental
            join fetch item.gameCopy copy
            join fetch copy.game
            where item.id = :itemId
            """)
    Optional<RentalItem> findDetailsById(@Param("itemId") Long itemId);

    @Query("""
            select case when count(item) > 0 then true else false end
            from RentalItem item
            where item.rental.user.id = :userId
              and item.gameCopy.game.id = :gameId
              and item.status <> :activeStatus
            """)
    boolean existsCompletedRentalForGame(
            @Param("userId") Long userId,
            @Param("gameId") Long gameId,
            @Param("activeStatus") RentalItemStatus activeStatus);
}
