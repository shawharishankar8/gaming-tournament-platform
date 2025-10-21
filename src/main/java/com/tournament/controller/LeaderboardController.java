package com.tournament.controller;

import com.tournament.model.entity.Player;
import com.tournament.repository.PlayerRepository;
import com.tournament.service.LeaderboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;
    private final PlayerRepository playerRepository;

    public LeaderboardController(LeaderboardService leaderboardService, PlayerRepository playerRepository) {
        this.leaderboardService = leaderboardService;
        this.playerRepository = playerRepository;
    }

    @GetMapping("/global")
    public List<Player> global(@RequestParam(defaultValue = "10") int limit) {
        var ids = leaderboardService.topPlayers(limit);
        return ids.stream()
                .map(playerRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
    }
}


