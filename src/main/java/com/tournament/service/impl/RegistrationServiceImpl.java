package com.tournament.service.impl;

import com.tournament.model.entity.Player;
import com.tournament.model.entity.Tournament;
import com.tournament.model.entity.TournamentRegistration;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.repository.PlayerRepository;
import com.tournament.repository.TournamentRegistrationRepository;
import com.tournament.repository.TournamentRepository;
import com.tournament.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TournamentRegistrationRepository registrationRepository;

    public RegistrationServiceImpl(TournamentRepository tournamentRepository,
                                   PlayerRepository playerRepository,
                                   TournamentRegistrationRepository registrationRepository) {
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public void register(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow();
        if (tournament.getStatus() != TournamentStatus.UPCOMING) {
            throw new IllegalStateException("Tournament not open for registration");
        }
        long registered = registrationRepository.countByTournament(tournament);
        if (registered >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Tournament is full");
        }
        Player player = playerRepository.findById(playerId).orElseThrow();
        TournamentRegistration reg = new TournamentRegistration();
        reg.setTournament(tournament);
        reg.setPlayer(player);
        registrationRepository.save(reg);
    }
}


