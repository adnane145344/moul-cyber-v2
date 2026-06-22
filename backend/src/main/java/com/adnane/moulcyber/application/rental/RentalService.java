package com.adnane.moulcyber.application.rental;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import com.adnane.moulcyber.application.catalog.GameNotFoundException;
import com.adnane.moulcyber.domain.catalog.Game;
import com.adnane.moulcyber.domain.inventory.GameCopy;
import com.adnane.moulcyber.domain.inventory.GameCopyStatus;
import com.adnane.moulcyber.domain.rental.Rental;
import com.adnane.moulcyber.domain.rental.RentalItem;
import com.adnane.moulcyber.domain.user.User;
import com.adnane.moulcyber.infra.persistence.catalog.GameRepository;
import com.adnane.moulcyber.infra.persistence.inventory.GameCopyRepository;
import com.adnane.moulcyber.infra.persistence.rental.RentalRepository;
import com.adnane.moulcyber.infra.persistence.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RentalService {

    private static final int RENTAL_DURATION_DAYS = 7;

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameCopyRepository gameCopyRepository;
    private final RentalRepository rentalRepository;
    private final RentalAssembler assembler;
    private final Clock clock;

    public RentalService(
            UserRepository userRepository,
            GameRepository gameRepository,
            GameCopyRepository gameCopyRepository,
            RentalRepository rentalRepository,
            RentalAssembler assembler,
            Clock clock) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.gameCopyRepository = gameCopyRepository;
        this.rentalRepository = rentalRepository;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Transactional
    public RentalResponse createRental(Long userId, CreateRentalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(com.adnane.moulcyber.application.user.UserNotFoundException::new);
        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(GameNotFoundException::new);
        GameCopy copy = gameCopyRepository
                .findFirstByGameIdAndStatusOrderByIdAsc(
                        game.getId(), GameCopyStatus.AVAILABLE)
                .orElseThrow(GameUnavailableException::new);

        LocalDate startDate = LocalDate.now(clock);
        Rental rental = new Rental(user, startDate, startDate.plusDays(RENTAL_DURATION_DAYS));
        copy.rent();
        rental.addItem(new RentalItem(copy, game.getRentalPrice()));

        return assembler.toResponse(rentalRepository.saveAndFlush(rental));
    }

    @Transactional(readOnly = true)
    public List<RentalSummaryResponse> findUserRentals(Long userId) {
        return rentalRepository.findDistinctByUserIdOrderByStartDateDescIdDesc(userId)
                .stream()
                .map(assembler::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public RentalResponse findUserRental(Long userId, Long rentalId) {
        Rental rental = rentalRepository.findDistinctByIdAndUserId(rentalId, userId)
                .orElseThrow(RentalNotFoundException::new);
        return assembler.toResponse(rental);
    }
}
