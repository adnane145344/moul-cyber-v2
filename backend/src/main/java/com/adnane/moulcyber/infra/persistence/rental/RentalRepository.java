package com.adnane.moulcyber.infra.persistence.rental;

import java.util.List;

import com.adnane.moulcyber.domain.rental.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserId(Long userId);

    List<Rental> findByUserIdOrderByStartDateDesc(Long userId);
}
