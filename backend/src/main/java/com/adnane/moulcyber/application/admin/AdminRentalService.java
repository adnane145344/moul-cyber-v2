package com.adnane.moulcyber.application.admin;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.adnane.moulcyber.application.rental.RentalNotFoundException;
import com.adnane.moulcyber.application.shared.PageResponse;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItemStatus;
import com.adnane.moulcyber.infra.persistence.rental.RentalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminRentalService {

    private final RentalRepository rentalRepository;
    private final AdminRentalAssembler assembler;
    private final Clock clock;

    public AdminRentalService(
            RentalRepository rentalRepository,
            AdminRentalAssembler assembler,
            Clock clock) {
        this.rentalRepository = rentalRepository;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminRentalSummaryResponse> findRentals(
            AdminRentalFilter filter,
            Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        Page<Long> rentalIds = findRentalIds(filter, today, pageable);
        List<Rental> rentals = findRentalsInPageOrder(rentalIds.getContent());
        List<AdminRentalSummaryResponse> content = rentals.stream()
                .map(rental -> assembler.toSummary(rental, today))
                .toList();
        return PageResponse.from(rentalIds, content);
    }

    @Transactional(readOnly = true)
    public AdminRentalDetailsResponse findRental(Long rentalId) {
        Rental rental = rentalRepository.findDetailedById(rentalId)
                .orElseThrow(RentalNotFoundException::new);
        return assembler.toDetails(rental, LocalDate.now(clock));
    }

    private Page<Long> findRentalIds(
            AdminRentalFilter filter,
            LocalDate today,
            Pageable pageable) {
        if (filter == null) {
            return rentalRepository.findAllIds(pageable);
        }
        return switch (filter) {
            case ACTIVE -> rentalRepository.findActiveIds(
                    RentalItemStatus.ACTIVE, today, pageable);
            case OVERDUE -> rentalRepository.findOverdueIds(
                    RentalItemStatus.ACTIVE, today, pageable);
            case COMPLETED -> rentalRepository.findCompletedIds(
                    RentalItemStatus.ACTIVE, pageable);
        };
    }

    private List<Rental> findRentalsInPageOrder(List<Long> rentalIds) {
        if (rentalIds.isEmpty()) {
            return List.of();
        }
        Map<Long, Integer> positions = java.util.stream.IntStream.range(0, rentalIds.size())
                .boxed()
                .collect(Collectors.toMap(rentalIds::get, Function.identity()));
        return rentalRepository.findDetailedByIdIn(rentalIds).stream()
                .sorted(Comparator.comparingInt(rental ->
                        positions.getOrDefault(rental.getId(), Integer.MAX_VALUE)))
                .toList();
    }
}
