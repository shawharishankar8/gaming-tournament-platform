package com.tournament.model.entity;

import com.tournament.model.enums.TournamentStatus;
import com.tournament.model.enums.TournamentType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tournaments")
public class Tournament extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentType type;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    private BigDecimal entryFee;

    @Column(nullable = false)
    private BigDecimal prizePool;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status = TournamentStatus.UPCOMING;

    @Column(nullable = false)
    private OffsetDateTime startTime;

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
    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }
    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }
}


