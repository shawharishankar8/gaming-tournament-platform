package com.tournament.service.impl;

import com.tournament.model.entity.Match;
import com.tournament.model.entity.Player;
import com.tournament.repository.MatchRepository;
import com.tournament.repository.PlayerRepository;
import com.tournament.service.MatchService;
import com.tournament.service.LeaderboardService;
import com.tournament.util.EloCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final LeaderboardService leaderboardService;

    public MatchServiceImpl(MatchRepository matchRepository, PlayerRepository playerRepository, LeaderboardService leaderboardService) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.leaderboardService = leaderboardService;
    }

    @Override
    @Transactional
    public void submitResult(Long matchId, Long winnerPlayerId) {
        Match match = matchRepository.findById(matchId).orElseThrow();
        Player player1 = match.getPlayer1();
        Player player2 = match.getPlayer2();
        Player winner = playerRepository.findById(winnerPlayerId).orElseThrow();
        if (!winner.getId().equals(player1.getId()) && !winner.getId().equals(player2.getId())) {
            throw new IllegalArgumentException("Winner must be participant of the match");
        }
        match.setWinner(winner);
        matchRepository.save(match);

        Player loser = winner.getId().equals(player1.getId()) ? player2 : player1;

        int kWinner = winner.getWins() + winner.getLosses() < 30 ? 32 : 16;
        int kLoser = loser.getWins() + loser.getLosses() < 30 ? 32 : 16;

        int newWinnerRating = EloCalculator.newRating(winner.getEloRating(), 1.0, loser.getEloRating(), kWinner);
        int newLoserRating = EloCalculator.newRating(loser.getEloRating(), 0.0, winner.getEloRating(), kLoser);

        winner.setEloRating(newWinnerRating);
        winner.setWins(winner.getWins() + 1);
        loser.setEloRating(newLoserRating);
        loser.setLosses(loser.getLosses() + 1);

        playerRepository.save(winner);
        playerRepository.save(loser);

        leaderboardService.updatePlayerScore(winner.getId(), winner.getEloRating());
        leaderboardService.updatePlayerScore(loser.getId(), loser.getEloRating());

        // If this was the last unresolved match in the round, generate next round
        long remaining = matchRepository.countByTournamentAndRoundNumberAndWinnerIsNull(match.getTournament(), match.getRoundNumber());
        if (remaining == 0) {
            var roundMatches = matchRepository.findByTournamentAndRoundNumberOrderByIdAsc(match.getTournament(), match.getRoundNumber());
            // Collect winners in order
            var winners = roundMatches.stream().map(Match::getWinner).toList();
            // If only one winner remains, tournament is complete
            if (winners.size() == 1) {
                return; // Final winner determined
            }
            // Pair winners into next round
            int nextRound = match.getRoundNumber() + 1;
            for (int i = 0; i < winners.size(); i += 2) {
                Match nm = new Match();
                nm.setTournament(match.getTournament());
                nm.setPlayer1(winners.get(i));
                if (i + 1 < winners.size()) {
                    nm.setPlayer2(winners.get(i + 1));
                } else {
                    // odd -> bye
                    nm.setPlayer2(winners.get(i));
                    nm.setWinner(winners.get(i));
                }
                nm.setRoundNumber(nextRound);
                matchRepository.save(nm);
            }
        }
    }
}


