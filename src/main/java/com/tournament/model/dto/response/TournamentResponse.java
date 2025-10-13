package com.tournament.model.dto.response;

import com.tournament.model.entity.Tournament;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.model.enums.TournamentType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TournamentResponse {
    private Long id;
    private String name;
    private TournamentType type;
    private Integer maxParticipants;
    private BigDecimal entryFee;
    private BigDecimal prizePool;
    private TournamentStatus status;
    private OffsetDateTime startTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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

    public static TournamentResponse from(Tournament t) {
        TournamentResponse r = new TournamentResponse();
        r.setId(t.getId());
        r.setName(t.getName());
        r.setType(t.getType());
        r.setMaxParticipants(t.getMaxParticipants());
        r.setEntryFee(t.getEntryFee());
        r.setPrizePool(t.getPrizePool());
        r.setStatus(t.getStatus());
        r.setStartTime(t.getStartTime());
        return r;
    }
}


