package com.tournament.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class Player extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true)
    private String gamerTag;

    @Column(nullable = false)
    private int eloRating = 1200;

    @Column(nullable = false)
    private int wins = 0;

    @Column(nullable = false)
    private int losses = 0;

    // Constructors
    public Player() {}

    public Player(User user, String gamerTag) {
        this.user = user;
        this.gamerTag = gamerTag;
    }

    public Player(String gamerTag) {
        this.gamerTag = gamerTag;
    }

    // Helper method to calculate win rate
    public double getWinRate() {
        int totalGames = wins + losses;
        return totalGames > 0 ? (double) wins / totalGames * 100 : 0.0;
    }

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getGamerTag() { return gamerTag; }
    public void setGamerTag(String gamerTag) { this.gamerTag = gamerTag; }

    public int getEloRating() { return eloRating; }
    public void setEloRating(int eloRating) { this.eloRating = eloRating; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
}