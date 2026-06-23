package com.adnane.moulcyber.infra.persistence.rental;

import java.util.List;
import java.util.Optional;

import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(
            value = """
                    select rental.id
                    from Rental rental
                    order by rental.startDate desc, rental.id desc
                    """,
            countQuery = "select count(rental) from Rental rental")
    Page<Long> findAllIds(Pageable pageable);

    @Query(
            value = """
                    select rental.id
                    from Rental rental
                    where exists (
                        select item.id
                        from RentalItem item
                        where item.rental = rental
                          and item.status = :activeStatus
                    )
                      and rental.dueDate >= :today
                    order by rental.startDate desc, rental.id desc
                    """,
            countQuery = """
                    select count(rental)
                    from Rental rental
                    where exists (
                        select item.id
                        from RentalItem item
                        where item.rental = rental
                          and item.status = :activeStatus
                    )
                      and rental.dueDate >= :today
                    """)
    Page<Long> findActiveIds(
            @Param("activeStatus") RentalItemStatus activeStatus,
            @Param("today") java.time.LocalDate today,
            Pageable pageable);

    @Query(
            value = """
                    select rental.id
                    from Rental rental
                    where exists (
                        select item.id
                        from RentalItem item
                        where item.rental = rental
                          and item.status = :activeStatus
                    )
                      and rental.dueDate < :today
                    order by rental.startDate desc, rental.id desc
                    """,
            countQuery = """
                    select count(rental)
                    from Rental rental
                    where exists (
                        select item.id
                        from RentalItem item
                        where item.rental = rental
                          and item.status = :activeStatus
                    )
                      and rental.dueDate < :today
                    """)
    Page<Long> findOverdueIds(
            @Param("activeStatus") RentalItemStatus activeStatus,
            @Param("today") java.time.LocalDate today,
            Pageable pageable);

    @Query(
            value = """
                    select rental.id
                    from Rental rental
                    where not exists (
                        select item.id
                        from RentalItem item
                        where item.rental = rental
                          and item.status = :activeStatus
                    )
                    order by rental.startDate desc, rental.id desc
                    """,
            countQuery = """
                    select count(rental)
                    from Rental rental
                    where not exists (
                        select item.id
                        from RentalItem item
                        where item.rental = rental
                          and item.status = :activeStatus
                    )
                    """)
    Page<Long> findCompletedIds(
            @Param("activeStatus") RentalItemStatus activeStatus,
            Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.gameCopy", "items.gameCopy.game"})
    @Query("select distinct rental from Rental rental where rental.id in :rentalIds")
    List<Rental> findDetailedByIdIn(@Param("rentalIds") List<Long> rentalIds);

    @EntityGraph(attributePaths = {"user", "items", "items.gameCopy", "items.gameCopy.game"})
    @Query("select rental from Rental rental where rental.id = :rentalId")
    Optional<Rental> findDetailedById(@Param("rentalId") Long rentalId);
}
