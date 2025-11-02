package com.tournament.service.impl;

import com.tournament.model.dto.request.CreatePaymentIntentRequest;
import com.tournament.model.dto.response.PaymentIntentResponse;
import com.tournament.model.entity.*;
import com.tournament.model.enums.PaymentStatus;
import com.tournament.model.enums.PaymentType;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.repository.PaymentRepository;
import com.tournament.repository.TournamentRepository;
import com.tournament.repository.TournamentRegistrationRepository;
import com.tournament.repository.UserRepository;
import com.tournament.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    private final PaymentRepository paymentRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              TournamentRepository tournamentRepository,
                              TournamentRegistrationRepository registrationRepository,
                              UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.tournamentRepository = tournamentRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;

        // ADD COMPREHENSIVE STRIPE DEBUG LOGGING
        logger.info("=== STRIPE INITIALIZATION DEBUG ===");
        logger.info("Stripe Secret Key from @Value: {}",
                stripeSecretKey != null && !stripeSecretKey.isEmpty() ? "PRESENT" : "NULL/EMPTY");

        // Check environment variable directly
        String stripeKeyFromEnv = System.getenv("STRIPE_SECRET_KEY");
        logger.info("Stripe Key from ENV: {}",
                stripeKeyFromEnv != null && !stripeKeyFromEnv.isEmpty() ? "PRESENT" : "NULL/EMPTY");

        // Use environment variable if @Value is empty
        String finalStripeKey = stripeSecretKey;
        if ((stripeSecretKey == null || stripeSecretKey.isEmpty()) &&
                stripeKeyFromEnv != null && !stripeKeyFromEnv.isEmpty()) {
            logger.info("Using Stripe key from environment variable");
            finalStripeKey = stripeKeyFromEnv;
        }

        if (finalStripeKey != null && !finalStripeKey.isEmpty()) {
            logger.info("Stripe Key Length: {}", finalStripeKey.length());
            logger.info("Stripe Key Preview: {}...",
                    finalStripeKey.substring(0, Math.min(8, finalStripeKey.length())));

            try {
                Stripe.apiKey = finalStripeKey;
                logger.info("Stripe API initialized successfully");

                // Test Stripe connectivity
                testStripeConnectivity();

            } catch (Exception e) {
                logger.error("Failed to initialize Stripe: {}", e.getMessage(), e);
            }
        } else {
            logger.error("STRIPE SECRET KEY IS EMPTY OR NULL! Check your configuration.");
        }
    }

    @Override
    @CircuitBreaker(name = "stripeService", fallbackMethod = "createPaymentIntentFallback")
    @Transactional
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request, User user) throws StripeException {
        logger.info("=== PAYMENT INTENT CREATION STARTED ===");
        logger.info("User: {}, Tournament ID: {}, Amount: {}",
                user.getUsername(), request.getTournamentId(), request.getAmount());

        try {
            Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                    .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

            logger.info("Tournament found: {} (Status: {})", tournament.getName(), tournament.getStatus());

            // Validate tournament status
            if (tournament.getStatus() != TournamentStatus.UPCOMING) {
                throw new IllegalStateException("Tournament is not open for registration");
            }

            // Check if user already has a successful payment for this tournament
            boolean hasPaid = paymentRepository.existsByUserAndTournamentAndTypeAndStatus(
                    user, tournament, PaymentType.ENTRY_FEE, PaymentStatus.SUCCEEDED);
            logger.info("User already paid for tournament: {}", hasPaid);

            if (hasPaid) {
                throw new IllegalStateException("User already paid entry fee for this tournament");
            }

            // Create payment record
            Payment payment = new Payment();
            payment.setUser(user);
            payment.setTournament(tournament);
            payment.setAmount(request.getAmount());
            payment.setType(PaymentType.ENTRY_FEE);
            payment.setDescription("Entry fee for tournament: " + tournament.getName());
            payment.setStatus(PaymentStatus.PENDING);

            logger.info("Creating Stripe Payment Intent with amount: {} cents",
                    convertToStripeAmount(request.getAmount()));

            // Create Stripe Payment Intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(convertToStripeAmount(request.getAmount()))
                    .setCurrency("usd")
                    .setDescription(payment.getDescription())
                    .putMetadata("tournament_id", tournament.getId().toString())
                    .putMetadata("user_id", user.getId().toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            logger.info("Calling Stripe API to create payment intent...");
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            logger.info("Stripe Payment Intent created: {}", paymentIntent.getId());

            // Update payment with Stripe details
            payment.setStripePaymentIntentId(paymentIntent.getId());
            paymentRepository.save(payment);
            logger.info("Payment record saved with ID: {}", payment.getId());

            PaymentIntentResponse response = new PaymentIntentResponse(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus()
            );

            logger.info("=== PAYMENT INTENT CREATION SUCCESS ===");
            return response;

        } catch (StripeException e) {
            logger.error("STRIPE EXCEPTION: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            logger.error("Stripe error details: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("EXCEPTION in createPaymentIntent: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public Payment handlePaymentSuccess(String paymentIntentId) {
        logger.info("Handling payment success for: {}", paymentIntentId);
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);

        // Create tournament registration if this is an entry fee payment
        if (payment.getType() == PaymentType.ENTRY_FEE) {
            createTournamentRegistration(payment);
        }

        return payment;
    }

    @Override
    @Transactional
    public Payment handlePaymentFailure(String paymentIntentId, String failureMessage) {
        logger.info("Handling payment failure for: {} - {}", paymentIntentId, failureMessage);
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureMessage(failureMessage);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void processStripeWebhook(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    PaymentIntent succeededIntent = (PaymentIntent) event.getData().getObject();
                    handlePaymentSuccess(succeededIntent.getId());
                    break;

                case "payment_intent.payment_failed":
                    PaymentIntent failedIntent = (PaymentIntent) event.getData().getObject();
                    String failureMessage = failedIntent.getLastPaymentError() != null ?
                            failedIntent.getLastPaymentError().getMessage() : "Payment failed";
                    handlePaymentFailure(failedIntent.getId(), failureMessage);
                    break;

                default:
                    logger.info("Unhandled event type: {}", event.getType());
            }
        } catch (Exception e) {
            logger.error("Webhook error: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook error: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<Payment> distributeTournamentPrizes(Tournament tournament) {
        if (tournament.getStatus() != TournamentStatus.COMPLETED) {
            throw new IllegalStateException("Tournament must be completed to distribute prizes");
        }

        // This is a simplified prize distribution - in reality, you'd have complex logic
        // based on tournament standings, bracket results, etc.
        List<Payment> prizePayments = new ArrayList<>();

        // Example: Distribute 70% to 1st, 20% to 2nd, 10% to 3rd
        // You would replace this with your actual tournament results
        BigDecimal totalPrizePool = tournament.getPrizePool();

        // For now, return empty list - implement based on your tournament results
        return prizePayments;
    }

    @Override
    @Transactional
    public Payment refundPayment(Long paymentId, BigDecimal amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Only succeeded payments can be refunded");
        }

        try {
            // Create refund in Stripe
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(
                    com.stripe.param.RefundCreateParams.builder()
                            .setPaymentIntent(payment.getStripePaymentIntentId())
                            .setAmount(convertToStripeAmount(amount))
                            .build()
            );

            payment.setStatus(PaymentStatus.REFUNDED);
            return paymentRepository.save(payment);

        } catch (StripeException e) {
            throw new RuntimeException("Refund failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment getPaymentByIntentId(String paymentIntentId) {
        return paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    // Fallback method for circuit breaker
    public PaymentIntentResponse createPaymentIntentFallback(CreatePaymentIntentRequest request, User user, Exception e) {
        logger.error("CIRCUIT BREAKER FALLBACK TRIGGERED: {}", e.getMessage());
        PaymentIntentResponse response = new PaymentIntentResponse();
        response.setStatus("failed");
        response.setMessage("Payment service is temporarily unavailable. Please try again later.");
        return response;
    }

    private long convertToStripeAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private void createTournamentRegistration(Payment payment) {
        // Find player associated with user
        Player player = payment.getUser().getPlayer();
        if (player == null) {
            throw new IllegalStateException("User does not have a player profile");
        }

        // Check if registration already exists
        if (registrationRepository.existsByTournamentAndPlayer(payment.getTournament(), player)) {
            throw new IllegalStateException("Player already registered for this tournament");
        }

        TournamentRegistration registration = new TournamentRegistration();
        registration.setTournament(payment.getTournament());
        registration.setPlayer(player);
        registration.setPayment(payment);
        registration.setPaymentStatus(com.tournament.model.enums.PaymentStatus.SUCCEEDED);

        registrationRepository.save(registration);
    }

    // Test Stripe connectivity
    private void testStripeConnectivity() {
        try {
            logger.info("Testing Stripe connectivity...");
            Map<String, Object> params = new HashMap<>();
            com.stripe.model.Balance balance = com.stripe.model.Balance.retrieve();
            logger.info("Stripe connectivity test: SUCCESS - API is reachable");
        } catch (Exception e) {
            logger.error("Stripe connectivity test: FAILED - {}", e.getMessage());
        }
    }
}