package com.tournament.controller;

import com.tournament.service.LeaderboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/global")
    public List<Long> global(@RequestParam(defaultValue = "10") int limit) {
        return leaderboardService.topPlayers(limit);
    }
}


