package com.tournament.service.impl;

import com.tournament.model.dto.request.TournamentCreateRequest;
import com.tournament.model.entity.Tournament;
import com.tournament.repository.TournamentRepository;
import com.tournament.service.TournamentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.OffsetDateTime;

@Service
public class TournamentServiceImpl implements TournamentService {
    private final TournamentRepository tournamentRepository;

    public TournamentServiceImpl(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public Tournament create(TournamentCreateRequest request) {
        Tournament t = new Tournament();
        t.setName(request.getName());
        t.setType(request.getType());
        t.setMaxParticipants(request.getMaxParticipants());
        t.setEntryFee(request.getEntryFee());
        t.setPrizePool(request.getPrizePool());
        t.setStartTime(OffsetDateTime.parse(request.getStartTime()));
        return tournamentRepository.save(t);
    }

    @Override
    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    @Override
    public Tournament findById(Long id) {
        return tournamentRepository.findById(id).orElseThrow();
    }
}


