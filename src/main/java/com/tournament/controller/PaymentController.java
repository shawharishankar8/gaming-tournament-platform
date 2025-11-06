package com.tournament.controller;

import com.stripe.Stripe;
import com.tournament.model.dto.request.CreatePaymentIntentRequest;
import com.tournament.model.dto.response.PaymentIntentResponse;
import com.tournament.model.entity.Payment;
import com.tournament.model.entity.Tournament;
import com.tournament.model.entity.User;
import com.tournament.model.enums.PaymentStatus;
import com.tournament.model.enums.PaymentType;
import com.tournament.repository.PaymentRepository;
import com.tournament.repository.UserRepository;
import com.tournament.repository.TournamentRepository;
import com.tournament.service.PaymentService;
import com.tournament.service.TournamentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.hibernate.validator.internal.util.logging.Log;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final TournamentService tournamentService;
    public final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;
    public final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService, TournamentService tournamentService,UserRepository userRepository,TournamentRepository tournamentRepository,PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.tournamentService = tournamentService;
        this.userRepository= userRepository;
        this.tournamentRepository =  tournamentRepository;
        this.paymentRepository= paymentRepository;
    }
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @PostMapping("/create-intent")
    @Operation(summary = "Create payment intent for tournament entry")
    public ResponseEntity<?> createPaymentIntent(
            @RequestBody @Valid CreatePaymentIntentRequest request,
            Principal principal) {

        try {
            // Get the authenticated user
            User user = getCurrentUser(principal);

            PaymentIntentResponse response = paymentService.createPaymentIntent(request, user);
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Stripe error in createPaymentIntent: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Payment service error: " + e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Validation error in createPaymentIntent: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Authentication error in createPaymentIntent: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        } catch (Exception e) {
            logger.error("Unexpected error in createPaymentIntent: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
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
    ///  test end point needs to be removed later
    @PostMapping("/test-payment")
    public ResponseEntity<?> testPaymentWithoutAuth() {
        try {
            // Get admin user directly
            User admin = userRepository.findByUsername("admin")
                    .orElseThrow(() -> new RuntimeException("Admin user not found"));

            // Get first tournament
            Tournament tournament = tournamentRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Tournament not found"));

            CreatePaymentIntentRequest request = new CreatePaymentIntentRequest();
            request.setTournamentId(tournament.getId());
            request.setAmount(tournament.getEntryFee());

            PaymentIntentResponse response = paymentService.createPaymentIntent(request, admin);

            return ResponseEntity.ok(Map.of(
                    "message", "Payment test successful",
                    "paymentIntent", response
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/test-payment-completion")
    public ResponseEntity<?> testPaymentCompletion(@RequestParam String paymentIntentId) {
        try {
            logger.info("=== TESTING COMPLETE PAYMENT FLOW ===");

            // Call the service method that handles payment success
            Payment payment = paymentService.handlePaymentSuccess(paymentIntentId);

            // Use PaymentRepository methods to verify payment status and check for existing payments
            boolean hasSuccessfulRegistrationPayment = false;
            String paymentAnalysis = "";

            if (payment.getTournament() != null && payment.getUser() != null) {
                // Check if there's already a successful registration payment for this user+tournament
                hasSuccessfulRegistrationPayment = paymentRepository.existsByUserAndTournamentAndTypeAndStatus(
                        payment.getUser(),
                        payment.getTournament(),
                        PaymentType.REGISTRATION, // or whatever your registration payment type is
                        PaymentStatus.SUCCEEDED
                );

                // Get all payments for this user with SUCCEEDED status
                List<Payment> userSuccessfulPayments = paymentRepository.findByUserAndStatus(
                        payment.getUser(),
                        PaymentStatus.SUCCEEDED
                );

                paymentAnalysis = String.format(
                        "User has %d successful payments. Registration payment exists: %s",
                        userSuccessfulPayments.size(),
                        hasSuccessfulRegistrationPayment
                );
            }

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Payment completion test successful");
            result.put("paymentId", payment.getId());
            result.put("paymentStatus", payment.getStatus());
            result.put("paymentType", payment.getType());
            result.put("stripePaymentIntentId", payment.getStripePaymentIntentId());
            result.put("amount", payment.getAmount());
           // result.put("currency", payment.getCurrency());

            // User and Tournament info
            if (payment.getUser() != null) {
                result.put("userId", payment.getUser().getId());
                result.put("userEmail", payment.getUser().getEmail());
            }

            if (payment.getTournament() != null) {
                result.put("tournamentId", payment.getTournament().getId());
                result.put("tournamentName", payment.getTournament().getName());

                // Calculate total successful payments for this tournament of this type
                BigDecimal totalTournamentRevenue = paymentRepository.sumAmountByTournamentAndTypeAndStatus(
                        payment.getTournament(),
                        payment.getType()
                );
                result.put("totalTournamentRevenueForType", totalTournamentRevenue);
            }

            result.put("hasSuccessfulRegistrationPayment", hasSuccessfulRegistrationPayment);
            result.put("paymentAnalysis", paymentAnalysis);
            result.put("registrationEligible",
                    payment.getStatus() == PaymentStatus.SUCCEEDED &&
                            payment.getType() == PaymentType.REGISTRATION && // Adjust based on your PaymentType enum
                            hasSuccessfulRegistrationPayment
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Payment completion test failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "message", "Payment completion test failed",
                    "timestamp", LocalDateTime.now()
            ));
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
        if (principal == null) {
            throw new RuntimeException("User not authenticated - no principal found");
        }

        String username = principal.getName();
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Invalid principal - username is empty");
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User Not Found: " + username));
    }

    }