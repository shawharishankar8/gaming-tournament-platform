package com.tournament.controller;

import com.tournament.model.dto.request.TournamentCreateRequest;
import com.tournament.model.dto.response.TournamentResponse;
import com.tournament.model.entity.Tournament;
import com.tournament.service.TournamentService;
import com.tournament.service.BracketGeneratorService;
import com.tournament.repository.MatchRepository;
import com.tournament.model.entity.Match;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final BracketGeneratorService bracketGeneratorService;
    private final MatchRepository matchRepository;

    public TournamentController(TournamentService tournamentService, BracketGeneratorService bracketGeneratorService, MatchRepository matchRepository) {
        this.tournamentService = tournamentService;
        this.bracketGeneratorService = bracketGeneratorService;
        this.matchRepository = matchRepository;
    }

    @PostMapping
    public ResponseEntity<TournamentResponse> create(@RequestBody TournamentCreateRequest request) {
        Tournament created = tournamentService.create(request);
        return ResponseEntity.created(URI.create("/api/tournaments/" + created.getId()))
                .body(TournamentResponse.from(created));
    }

    @GetMapping
    public List<TournamentResponse> list() {
        return tournamentService.findAll().stream().map(TournamentResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TournamentResponse get(@PathVariable Long id) {
        return TournamentResponse.from(tournamentService.findById(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> start(@PathVariable Long id) {
        bracketGeneratorService.generateFirstRound(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bracket")
    public List<Match> bracket(@PathVariable Long id) {
        Tournament t = tournamentService.findById(id);
        return matchRepository.findByTournamentOrderByRoundNumberAsc(t);
    }
}


