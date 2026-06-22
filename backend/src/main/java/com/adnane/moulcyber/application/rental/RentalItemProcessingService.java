package com.adnane.moulcyber.application.rental;

import java.time.Clock;
import java.time.LocalDate;

import com.adnane.moulcyber.domain.rental.RentalItem;
import com.adnane.moulcyber.infra.persistence.rental.RentalItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RentalItemProcessingService {

    private final RentalItemRepository rentalItemRepository;
    private final Clock clock;

    public RentalItemProcessingService(
            RentalItemRepository rentalItemRepository,
            Clock clock) {
        this.rentalItemRepository = rentalItemRepository;
        this.clock = clock;
    }

    @Transactional
    public RentalItemProcessingResponse returnItem(Long itemId) {
        RentalItem item = findActiveItem(itemId);
        item.returnOn(LocalDate.now(clock));
        return responseFor(item);
    }

    @Transactional
    public RentalItemProcessingResponse markLost(Long itemId) {
        RentalItem item = findActiveItem(itemId);
        item.markAsLost(LocalDate.now(clock));
        return responseFor(item);
    }

    @Transactional
    public RentalItemProcessingResponse markDamaged(Long itemId) {
        RentalItem item = findActiveItem(itemId);
        item.markAsDamaged(LocalDate.now(clock));
        return responseFor(item);
    }

    private RentalItem findActiveItem(Long itemId) {
        RentalItem item = rentalItemRepository.findDetailsById(itemId)
                .orElseThrow(RentalItemNotFoundException::new);
        if (!item.isActive()) {
            throw new RentalItemAlreadyProcessedException();
        }
        return item;
    }

    private RentalItemProcessingResponse responseFor(RentalItem item) {
        return new RentalItemProcessingResponse(
                item.getId(),
                item.getStatus(),
                item.getGameCopy().getStatus(),
                item.getProcessedDate(),
                item.getLateFee());
    }
}
