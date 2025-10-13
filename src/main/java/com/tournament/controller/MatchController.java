package com.tournament.controller;

import com.tournament.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/{matchId}/result/{winnerId}")
    public ResponseEntity<Void> submit(@PathVariable Long matchId, @PathVariable Long winnerId) {
        matchService.submitResult(matchId, winnerId);
        return ResponseEntity.noContent().build();
    }
}


