package com.tournament.controller;

import com.tournament.model.dto.request.CreatePaymentIntentRequest;
import com.tournament.model.dto.response.PaymentIntentResponse;
import com.tournament.model.entity.Payment;
import com.tournament.model.entity.Tournament;
import com.tournament.model.entity.User;
import com.tournament.repository.UserRepository;
import com.tournament.service.PaymentService;
import com.tournament.service.TournamentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final TournamentService tournamentService;
    public final UserRepository userRepository;

    public PaymentController(PaymentService paymentService, TournamentService tournamentService,UserRepository userRepository) {
        this.paymentService = paymentService;
        this.tournamentService = tournamentService;
        this.userRepository= userRepository;
    }

    @PostMapping("/create-intent")
    @Operation(summary = "Create payment intent for tournament entry")
    public ResponseEntity<?> createPaymentIntent(
            @RequestBody @Valid CreatePaymentIntentRequest request,
            Principal principal) {

        try {
            // In a real implementation, you'd get the user from the principal
            // For now, we'll use a placeholder - you'll need to implement user retrieval
            User user = getCurrentUser(principal);

            PaymentIntentResponse response = paymentService.createPaymentIntent(request, user);
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    @Operation(summary = "Handle Stripe webhook events")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        try {
            paymentService.processStripeWebhook(payload, signature);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
        }
    }

    @PostMapping("/tournaments/{tournamentId}/distribute-prizes")
    @Operation(summary = "Distribute prizes for completed tournament")
    public ResponseEntity<?> distributePrizes(@PathVariable Long tournamentId) {
        try {
            Tournament tournament = tournamentService.findById(tournamentId);
            List<Payment> prizePayments = paymentService.distributeTournamentPrizes(tournament);

            return ResponseEntity.ok(Map.of(
                    "message", "Prizes distributed successfully",
                    "paymentsCount", prizePayments.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<?> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) BigDecimal amount) {

        try {
            Payment refundedPayment = paymentService.refundPayment(paymentId, amount);
            return ResponseEntity.ok(Map.of(
                    "message", "Refund processed successfully",
                    "paymentId", refundedPayment.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{paymentIntentId}")
    @Operation(summary = "Get payment by Stripe payment intent ID")
    public ResponseEntity<?> getPayment(@PathVariable String paymentIntentId) {
        try {
            Payment payment = paymentService.getPaymentByIntentId(paymentIntentId);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper method - you'll need to implement this based on your user retrieval logic
    private User getCurrentUser(Principal principal) {
     String username = principal.getName();
     return userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User Not Found"+ username));
    }
}