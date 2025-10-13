package com.tournament.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
public class Player extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String gamerTag;

    @Column(nullable = false)
    private int eloRating = 1200;

    @Column(nullable = false)
    private int wins = 0;

    @Column(nullable = false)
    private int losses = 0;

    public String getGamerTag() { return gamerTag; }
    public void setGamerTag(String gamerTag) { this.gamerTag = gamerTag; }
    public int getEloRating() { return eloRating; }
    public void setEloRating(int eloRating) { this.eloRating = eloRating; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
}


