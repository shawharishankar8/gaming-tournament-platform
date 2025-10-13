package com.tournament.service;

import java.util.List;

public interface LeaderboardService {
    void updatePlayerScore(long playerId, int eloRating);
    List<Long> topPlayers(int limit);
}


