package com.tournament.service.impl;

import com.tournament.model.entity.Tournament;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.repository.MatchRepository;
import com.tournament.repository.TournamentRepository;
import com.tournament.service.BracketGeneratorService;
import com.tournament.service.ScheduledTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ScheduledTaskServiceImpl implements ScheduledTaskService {
	private final TournamentRepository tournamentRepository;
	private final BracketGeneratorService bracketGeneratorService;
    private final MatchRepository matchRepository;

    public ScheduledTaskServiceImpl(TournamentRepository tournamentRepository,
                                    BracketGeneratorService bracketGeneratorService,
                                    MatchRepository matchRepository) {
		this.tournamentRepository = tournamentRepository;
		this.bracketGeneratorService = bracketGeneratorService;
        this.matchRepository = matchRepository;
	}

	@Override
	@Scheduled(cron = "0 0 2 * * *")
	public void runCleanup() {
		// placeholder for daily cleanup
	}

	@Override
	@Scheduled(fixedDelay = 300000)
	public void startDueTournaments() {
		List<Tournament> due = tournamentRepository.findByStatusAndStartTimeLessThanEqual(
				TournamentStatus.UPCOMING, OffsetDateTime.now());
		for (Tournament t : due) {
			t.setStatus(TournamentStatus.IN_PROGRESS);
			tournamentRepository.save(t);
			bracketGeneratorService.generateFirstRound(t.getId());
		}
	}

	@Override
	@Scheduled(fixedDelay = 600000)
	public void finalizeCompletedTournaments() {
        var inProgress = tournamentRepository.findByStatus(TournamentStatus.IN_PROGRESS);
        for (Tournament t : inProgress) {
            long total = matchRepository.countByTournament(t);
            if (total == 0) continue;
            long resolved = matchRepository.countByTournamentAndWinnerIsNotNull(t);
            if (resolved == total) {
                t.setStatus(TournamentStatus.COMPLETED);
                tournamentRepository.save(t);
            }
        }
	}
}
