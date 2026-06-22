package com.adnane.moulcyber.infra.persistence.rental;

import java.util.List;
import java.util.Optional;

import com.adnane.moulcyber.domain.rental.Rental;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"items", "items.gameCopy", "items.gameCopy.game"})
    List<Rental> findDistinctByUserIdOrderByStartDateDescIdDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "items.gameCopy", "items.gameCopy.game"})
    Optional<Rental> findDistinctByIdAndUserId(Long rentalId, Long userId);
}
