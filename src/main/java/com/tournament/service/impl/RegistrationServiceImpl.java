package com.tournament.service.impl;

import com.tournament.model.entity.Player;
import com.tournament.model.entity.Tournament;
import com.tournament.model.entity.TournamentRegistration;
import com.tournament.model.entity.User;
import com.tournament.model.enums.PaymentStatus;  // This is the correct enum
import com.tournament.model.enums.PaymentType;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.exception.DuplicateRegistrationException;
import com.tournament.repository.PaymentRepository;
import com.tournament.repository.PlayerRepository;
import com.tournament.repository.TournamentRegistrationRepository;
import com.tournament.repository.TournamentRepository;
import com.tournament.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final PaymentRepository paymentRepository;  // Added final

    public RegistrationServiceImpl(TournamentRepository tournamentRepository,
                                   PlayerRepository playerRepository,
                                   TournamentRegistrationRepository registrationRepository,
                                   PaymentRepository paymentRepository) {  // Added parameter
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
        this.registrationRepository = registrationRepository;
        this.paymentRepository = paymentRepository;  // Initialize the repository
    }

    @Override
    @Transactional
    public void register(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.UPCOMING) {
            throw new IllegalStateException("Tournament not open for registration");
        }

        long registered = registrationRepository.countByTournament(tournament);
        if (registered >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Tournament is full");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        if (registrationRepository.existsByTournamentAndPlayer(tournament, player)) {
            throw new DuplicateRegistrationException("Player already registered for this tournament");
        }

        // Check if payment is required and if it's been paid
        if (tournament.getEntryFee().compareTo(BigDecimal.ZERO) > 0) {
            // Look for successful payment for this tournament and player
            User user = player.getUser();
            if (user == null) {
                throw new IllegalStateException("Player is not associated with a user");
            }

            boolean hasPaid = paymentRepository.existsByUserAndTournamentAndTypeAndStatus(
                    user, tournament, PaymentType.ENTRY_FEE, PaymentStatus.SUCCEEDED);

            if (!hasPaid) {
                throw new IllegalStateException("Entry fee payment required before registration");
            }
        }

        TournamentRegistration reg = new TournamentRegistration();
        reg.setTournament(tournament);
        reg.setPlayer(player);

        // Set payment status based on entry fee - use the correct enum
        if (tournament.getEntryFee().compareTo(BigDecimal.ZERO) > 0) {
            reg.setPaymentStatus(PaymentStatus.SUCCEEDED);  // Use the correct enum value
        } else {
            reg.setPaymentStatus(PaymentStatus.NOT_REQUIRED);  // Use the correct enum value
        }

        registrationRepository.save(reg);
    }
}