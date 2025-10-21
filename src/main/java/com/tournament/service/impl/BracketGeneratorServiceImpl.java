package com.tournament.service.impl;

import com.tournament.model.entity.Match;
import com.tournament.model.entity.Player;
import com.tournament.model.entity.Tournament;
import com.tournament.repository.MatchRepository;
import com.tournament.repository.TournamentRegistrationRepository;
import com.tournament.repository.TournamentRepository;
import com.tournament.service.BracketGeneratorService;
import com.tournament.service.strategy.EloMatchmakingStrategy;
import com.tournament.service.strategy.MatchmakingContext;
import com.tournament.service.strategy.MatchmakingStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BracketGeneratorServiceImpl implements BracketGeneratorService {
    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;

    public BracketGeneratorServiceImpl(TournamentRepository tournamentRepository,
                                       TournamentRegistrationRepository registrationRepository,
                                       MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    @Transactional
    public void generateFirstRound(Long tournamentId) {
        Tournament t = tournamentRepository.findById(tournamentId).orElseThrow();
        List<Player> players = registrationRepository.findByTournament(t)
                .stream().map(r -> r.getPlayer()).collect(Collectors.toList());
        MatchmakingStrategy strategy = new EloMatchmakingStrategy();
        MatchmakingContext context = new MatchmakingContext(strategy);
        int round = 1;
        for (var pair : context.pair(players)) {
            Match m = new Match();
            m.setTournament(t);
            m.setPlayer1(pair[0]);
            if (pair[1] != null) {
                m.setPlayer2(pair[1]);
            } else {
                // bye -> auto-advance p1 by setting winner now
                m.setPlayer2(pair[0]);
                m.setWinner(pair[0]);
            }
            m.setRoundNumber(round);
            matchRepository.save(m);
        }
    }
}


