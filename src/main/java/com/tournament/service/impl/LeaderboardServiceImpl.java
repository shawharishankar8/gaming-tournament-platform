package com.tournament.service.impl;

import com.tournament.service.LeaderboardService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {
    private static final String GLOBAL_LEADERBOARD = "leaderboard:global";
    private final StringRedisTemplate redis;

    public LeaderboardServiceImpl(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void updatePlayerScore(long playerId, int eloRating) {
        redis.opsForZSet().add(GLOBAL_LEADERBOARD, String.valueOf(playerId), eloRating);
    }

    @Override
    public List<Long> topPlayers(int limit) {
        var members = redis.opsForZSet().reverseRange(GLOBAL_LEADERBOARD, 0, limit - 1);
        if (members == null) {
            return List.of();
        }
        return members.stream().map(Long::valueOf).collect(Collectors.toList());
    }
}


