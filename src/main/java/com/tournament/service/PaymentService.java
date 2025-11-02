package com.tournament.service;

import com.tournament.model.dto.request.CreatePaymentIntentRequest;
import com.tournament.model.dto.response.PaymentIntentResponse;
import com.tournament.model.entity.Payment;
import com.tournament.model.entity.Tournament;
import com.tournament.model.entity.User;
import com.stripe.exception.StripeException;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request, User user) throws StripeException;

    Payment handlePaymentSuccess(String paymentIntentId);

    Payment handlePaymentFailure(String paymentIntentId, String failureMessage);

    void processStripeWebhook(String payload, String signature);

    List<Payment> distributeTournamentPrizes(Tournament tournament);

    Payment refundPayment(Long paymentId, BigDecimal amount);

    Payment getPaymentByIntentId(String paymentIntentId);
}