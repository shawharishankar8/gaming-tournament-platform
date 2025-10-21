package com.tournament.controller;

import com.tournament.model.dto.request.TournamentCreateRequest;
import com.tournament.model.dto.response.TournamentResponse;
import com.tournament.model.entity.Tournament;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.model.dto.request.RegisterPlayerRequest;
import com.tournament.service.TournamentService;
import com.tournament.service.BracketGeneratorService;
import com.tournament.repository.MatchRepository;
import com.tournament.model.entity.Match;
import com.tournament.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
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
    private final RegistrationService registrationService;

    public TournamentController(TournamentService tournamentService, BracketGeneratorService bracketGeneratorService, MatchRepository matchRepository, RegistrationService registrationService) {
        this.tournamentService = tournamentService;
        this.bracketGeneratorService = bracketGeneratorService;
        this.matchRepository = matchRepository;
        this.registrationService = registrationService;
    }

    @PostMapping
    @Operation(summary = "Create a tournament")
    public ResponseEntity<TournamentResponse> create(@RequestBody TournamentCreateRequest request) {
        Tournament created = tournamentService.create(request);
        return ResponseEntity.created(URI.create("/api/tournaments/" + created.getId()))
                .body(TournamentResponse.from(created));
    }

    @GetMapping
    @Operation(summary = "List all tournaments")
    public List<TournamentResponse> list() {
        return tournamentService.findAll().stream().map(TournamentResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a tournament by id")
    public TournamentResponse get(@PathVariable Long id) {
        return TournamentResponse.from(tournamentService.findById(id));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a tournament and generate first round")
    public ResponseEntity<Void> start(@PathVariable Long id) {
        bracketGeneratorService.generateFirstRound(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bracket")
    @Operation(summary = "Get tournament bracket matches")
    public List<Match> bracket(@PathVariable Long id) {
        Tournament t = tournamentService.findById(id);
        return matchRepository.findByTournamentOrderByRoundNumberAsc(t);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get tournament status")
    public String status(@PathVariable Long id) {
        Tournament t = tournamentService.findById(id);
        return t.getStatus().name();
    }

    @GetMapping("/{id}/state")
    @Operation(summary = "Alias of status")
    public String state(@PathVariable Long id) {
        Tournament t = tournamentService.findById(id);
        return t.getStatus().name();
    }

    @PostMapping("/{id}/register")
    @Operation(summary = "Register a player into a tournament")
    public ResponseEntity<Void> register(@PathVariable Long id, @RequestBody RegisterPlayerRequest request) {
        registrationService.register(id, request.getPlayerId());
        return ResponseEntity.noContent().build();
    }
}


