package com.tournament.repository;

import com.tournament.model.entity.Tournament;
import com.tournament.model.entity.Player;
import com.tournament.model.entity.TournamentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, Long> {
    long countByTournament(Tournament tournament);
    List<TournamentRegistration> findByTournament(Tournament tournament);
    boolean existsByTournamentAndPlayer(Tournament tournament, Player player);
}


