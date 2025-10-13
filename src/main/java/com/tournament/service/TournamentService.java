package com.tournament.service;

import com.tournament.model.dto.request.TournamentCreateRequest;
import com.tournament.model.entity.Tournament;

import java.util.List;

public interface TournamentService {
    Tournament create(TournamentCreateRequest request);
    List<Tournament> findAll();
    Tournament findById(Long id);
}


