package com.adnane.moulcyber.infra.persistence.rental;

import java.util.List;
import java.util.Optional;

import com.adnane.moulcyber.domain.rental.Rental;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"items", "items.gameCopy", "items.gameCopy.game"})
    List<Rental> findDistinctByUserIdOrderByStartDateDescIdDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "items.gameCopy", "items.gameCopy.game"})
    Optional<Rental> findDistinctByIdAndUserId(Long rentalId, Long userId);

    @EntityGraph(attributePaths = {"user", "items", "items.gameCopy", "items.gameCopy.game"})
    List<Rental> findDistinctByOrderByStartDateDescIdDesc();

    @EntityGraph(attributePaths = {"user", "items", "items.gameCopy", "items.gameCopy.game"})
    @Query("select rental from Rental rental where rental.id = :rentalId")
    Optional<Rental> findDetailedById(@Param("rentalId") Long rentalId);
}
