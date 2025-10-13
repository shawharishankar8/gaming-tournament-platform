package com.tournament.util;

public class EloCalculator {
    public static int expectedScore(int ratingA, int ratingB) {
        double expected = 1.0 / (1.0 + Math.pow(10, (ratingB - ratingA) / 400.0));
        return (int) Math.round(expected * 1000);
    }

    public static int newRating(int oldRating, double actualScore, int opponentRating, int kFactor) {
        double expected = 1.0 / (1.0 + Math.pow(10, (opponentRating - oldRating) / 400.0));
        return (int) Math.round(oldRating + kFactor * (actualScore - expected));
    }
}


