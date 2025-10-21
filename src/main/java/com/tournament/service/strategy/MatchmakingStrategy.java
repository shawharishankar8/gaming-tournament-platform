package com.tournament.service.strategy;

import com.tournament.model.entity.Player;

import java.util.List;

public interface MatchmakingStrategy {
    List<Player[]> makePairs(List<Player> players);
}


