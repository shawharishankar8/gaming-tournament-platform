package com.tournament.service.strategy;

import com.tournament.model.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EloMatchmakingStrategy implements MatchmakingStrategy {
    @Override
    public List<Player[]> makePairs(List<Player> players) {
        List<Player> sorted = new ArrayList<>(players);
        sorted.sort(Comparator.comparingInt(Player::getEloRating));
        List<Player[]> result = new ArrayList<>();
        // Greedy pairing by nearest ELO
        while (!sorted.isEmpty()) {
            Player p1 = sorted.remove(0);
            if (sorted.isEmpty()) {
                result.add(new Player[]{p1, null});
                break;
            }
            // Find closest by rating
            int idx = 0;
            int bestDelta = Math.abs(sorted.get(0).getEloRating() - p1.getEloRating());
            for (int i = 1; i < sorted.size(); i++) {
                int d = Math.abs(sorted.get(i).getEloRating() - p1.getEloRating());
                if (d < bestDelta) { bestDelta = d; idx = i; }
            }
            Player p2 = sorted.remove(idx);
            result.add(new Player[]{p1, p2});
        }
        return result;
    }
}


