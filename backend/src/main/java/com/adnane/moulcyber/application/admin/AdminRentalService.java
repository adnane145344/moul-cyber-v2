package com.adnane.moulcyber.application.admin;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import com.adnane.moulcyber.application.rental.RentalNotFoundException;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalStatus;
import com.adnane.moulcyber.infra.persistence.rental.RentalRepository;
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
    public List<AdminRentalSummaryResponse> findRentals(AdminRentalFilter filter) {
        LocalDate today = LocalDate.now(clock);
        return rentalRepository.findDistinctByOrderByStartDateDescIdDesc().stream()
                .filter(rental -> matches(rental, filter, today))
                .map(rental -> assembler.toSummary(rental, today))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminRentalDetailsResponse findRental(Long rentalId) {
        Rental rental = rentalRepository.findDetailedById(rentalId)
                .orElseThrow(RentalNotFoundException::new);
        return assembler.toDetails(rental, LocalDate.now(clock));
    }

    private boolean matches(
            Rental rental,
            AdminRentalFilter filter,
            LocalDate today) {
        if (filter == null) {
            return true;
        }
        return switch (filter) {
            case ACTIVE ->
                    rental.getStatus() == RentalStatus.ACTIVE && !rental.isOverdueOn(today);
            case OVERDUE -> rental.isOverdueOn(today);
            case COMPLETED -> rental.getStatus() == RentalStatus.COMPLETED;
        };
    }
}
