package com.tournament.repository;

import com.tournament.model.entity.Match;
import com.tournament.model.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournamentOrderByRoundNumberAsc(Tournament tournament);
    List<Match> findByTournamentAndRoundNumberOrderByIdAsc(Tournament tournament, int roundNumber);
    long countByTournamentAndRoundNumberAndWinnerIsNull(Tournament tournament, int roundNumber);
    long countByTournament(Tournament tournament);
    long countByTournamentAndWinnerIsNotNull(Tournament tournament);
}


