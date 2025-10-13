package com.tournament.model.dto.request;

import com.tournament.model.enums.TournamentType;
import java.math.BigDecimal;

public class TournamentCreateRequest {
    private String name;
    private TournamentType type;
    private Integer maxParticipants;
    private BigDecimal entryFee;
    private BigDecimal prizePool;
    private String startTime; // ISO-8601 string

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TournamentType getType() { return type; }
    public void setType(TournamentType type) { this.type = type; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public BigDecimal getEntryFee() { return entryFee; }
    public void setEntryFee(BigDecimal entryFee) { this.entryFee = entryFee; }
    public BigDecimal getPrizePool() { return prizePool; }
    public void setPrizePool(BigDecimal prizePool) { this.prizePool = prizePool; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
}


