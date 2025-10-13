package com.tournament.repository;

import com.tournament.model.entity.Tournament;
import com.tournament.model.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByStatus(TournamentStatus status);
    List<Tournament> findByStatusAndStartTimeLessThanEqual(TournamentStatus status, OffsetDateTime startTime);
}


