package com.tournament.service.strategy;

import com.tournament.model.entity.Player;

import java.util.List;

public class MatchmakingContext {
    private MatchmakingStrategy strategy;

    public MatchmakingContext(MatchmakingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(MatchmakingStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Player[]> pair(List<Player> players) {
        return strategy.makePairs(players);
    }
}


