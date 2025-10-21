package com.tournament.service.strategy;

import com.tournament.model.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomMatchmakingStrategy implements MatchmakingStrategy {
    @Override
    public List<Player[]> makePairs(List<Player> players) {
        List<Player> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);
        List<Player[]> pairs = new ArrayList<>();
        for (int i = 0; i < shuffled.size(); i += 2) {
            Player p1 = shuffled.get(i);
            Player p2 = (i + 1 < shuffled.size()) ? shuffled.get(i + 1) : null;
            pairs.add(new Player[]{p1, p2});
        }
        return pairs;
    }
}


