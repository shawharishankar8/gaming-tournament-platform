package com.tournament.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class CreatePaymentIntentRequest {

    @NotNull
    private Long tournamentId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String paymentMethodId;

    // ADD THIS DEFAULT CONSTRUCTOR
    public CreatePaymentIntentRequest() {
    }

    // Getters and Setters
    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
}