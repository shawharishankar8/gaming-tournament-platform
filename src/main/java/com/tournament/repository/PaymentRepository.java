package com.tournament.repository;

import com.tournament.model.entity.Payment;
import com.tournament.model.entity.Tournament;
import com.tournament.model.entity.User;
import com.tournament.model.enums.PaymentStatus;
import com.tournament.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Payment> findByUserAndTournamentAndType(User user, Tournament tournament, PaymentType type);

    List<Payment> findByTournamentAndTypeAndStatus(Tournament tournament, PaymentType type, PaymentStatus status);

    List<Payment> findByUserAndStatus(User user, PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.tournament = :tournament AND p.type = :type AND p.status = 'SUCCEEDED'")
    BigDecimal sumAmountByTournamentAndTypeAndStatus(@Param("tournament") Tournament tournament,
                                                     @Param("type") PaymentType type);

    boolean existsByUserAndTournamentAndTypeAndStatus(User user, Tournament tournament,
                                                      PaymentType type, PaymentStatus status);
}
