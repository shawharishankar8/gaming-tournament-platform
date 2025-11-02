package com.tournament.model.entity;

import com.tournament.model.enums.PaymentStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "tournament_registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id", "player_id"})
})
public class TournamentRegistration extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id")
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column
    private Integer seedNumber;

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public Integer getSeedNumber() { return seedNumber; }
    public void setSeedNumber(Integer seedNumber) { this.seedNumber = seedNumber; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
}


