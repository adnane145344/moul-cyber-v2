package com.adnane.moulcyber.infra.persistence.catalog;

import java.util.List;

import com.adnane.moulcyber.domain.catalog.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findAllByOrderByTitleAscIdAsc();

    List<Game> findByTitleContainingIgnoreCaseOrderByTitleAscIdAsc(String title);
}
