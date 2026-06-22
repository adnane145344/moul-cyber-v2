package com.adnane.moulcyber.infra.persistence.rental;

import java.util.Optional;

import com.adnane.moulcyber.domain.rental.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {

    @Query("""
            select item
            from RentalItem item
            join fetch item.rental rental
            join fetch item.gameCopy copy
            join fetch copy.game
            where item.id = :itemId
            """)
    Optional<RentalItem> findDetailsById(@Param("itemId") Long itemId);
}
