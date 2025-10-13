package com.tournament.util;

import com.tournament.model.entity.Player;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BracketGenerator {
    public static List<Player[]> generateSingleEliminationPairs(List<Player> players) {
        List<Player> sorted = new ArrayList<>(players);
        sorted.sort(Comparator.comparingInt(Player::getEloRating).reversed());
        List<Player[]> pairs = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i += 2) {
            Player p1 = sorted.get(i);
            Player p2 = (i + 1 < sorted.size()) ? sorted.get(i + 1) : null;
            pairs.add(new Player[]{p1, p2});
        }
        return pairs;
    }
}


