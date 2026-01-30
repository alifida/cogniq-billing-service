package com.cognitivequantum.billing.service.payment;

import com.cognitivequantum.billing.client.AuthClient;
import com.cognitivequantum.billing.client.dto.AuthUserDto;
import com.cognitivequantum.billing.entity.PlanTier;
import com.cognitivequantum.billing.entity.Subscription;
import com.cognitivequantum.billing.entity.SubscriptionStatus;
import com.cognitivequantum.billing.exception.PaymentFailedException;
import com.cognitivequantum.billing.repository.SubscriptionRepository;
import com.cognitivequantum.billing.service.credit.CreditService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

	private static final String STRIPE_CIRCUIT_BREAKER = "stripe";

	private final SubscriptionRepository subscriptionRepository;
	private final CreditService creditService;
	private final AuthClient authClient;
	private final CircuitBreakerRegistry circuitBreakerRegistry;
	private final MeterRegistry meterRegistry;

	@Value("${cogniq.stripe.api-key:}")
	private String stripeApiKey;

	@Value("${cogniq.stripe.webhook-secret:}")
	private String webhookSecret;

	@Value("${cogniq.stripe.price-id.pro:}")
	private String priceIdPro;

	@Value("${cogniq.stripe.price-id.enterprise:}")
	private String priceIdEnterprise;

	@Value("${cogniq.billing.credits.pro:100}")
	private int creditsPro;

	@Value("${cogniq.billing.credits.enterprise:500}")
	private int creditsEnterprise;

	/**
	 * Create Stripe Checkout Session; returns URL to redirect user to Stripe hosted page.
	 * When circuit is open (Stripe down), throws PaymentFailedException with "Payment System Maintenance".
	 */
	public String createCheckoutSession(UUID userId, PlanTier planTier, String successUrl, String cancelUrl) {
		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(STRIPE_CIRCUIT_BREAKER);
		try {
			return cb.executeSupplier(() -> doCreateCheckoutSession(userId, planTier, successUrl, cancelUrl));
		} catch (CallNotPermittedException e) {
			meterRegistry.counter("payment_failure_total", "reason", "circuit_open").increment();
			throw new PaymentFailedException("Payment System Maintenance");
		}
	}

	private String doCreateCheckoutSession(UUID userId, PlanTier planTier, String successUrl, String cancelUrl) {
		if (planTier == PlanTier.FREE) {
			throw new PaymentFailedException("FREE tier does not require checkout");
		}
		String priceId = getPriceIdForTier(planTier);
		if (priceId == null || priceId.isBlank()) {
			throw new PaymentFailedException("Stripe price not configured for tier: " + planTier);
		}
		String customerEmail = null;
		AuthUserDto user = authClient.getUserById(userId);
		if (user != null && user.getEmail() != null) {
			customerEmail = user.getEmail();
		}
		SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
			.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
			.addLineItem(SessionCreateParams.LineItem.builder()
				.setPrice(priceId)
				.setQuantity(1L)
				.build())
			.setSuccessUrl(successUrl != null && !successUrl.isBlank() ? successUrl : "https://example.com/success")
			.setCancelUrl(cancelUrl != null && !cancelUrl.isBlank() ? cancelUrl : "https://example.com/cancel")
			.putMetadata("userId", userId.toString())
			.putMetadata("planTier", planTier.name());
		if (customerEmail != null) {
			paramsBuilder.setCustomerEmail(customerEmail);
		}
		try {
			Session session = Session.create(paramsBuilder.build());
			log.info("Stripe Checkout Session created: sessionId={}, userId={}, planTier={}", session.getId(), userId, planTier);
			return session.getUrl();
		} catch (StripeException e) {
			log.error("Stripe error creating checkout session: {}", e.getMessage());
			meterRegistry.counter("payment_failure_total", "reason", "stripe_error").increment();
			throw new PaymentFailedException("Payment system error: " + e.getMessage(), e);
		}
	}

	/**
	 * Verify Stripe webhook signature and handle event (e.g. invoice.paid).
	 */
	public void handleWebhook(String payload, String signature) {
		if (webhookSecret == null || webhookSecret.isBlank()) {
			log.warn("Stripe webhook secret not configured");
			return;
		}
		Event event;
		try {
			event = Webhook.constructEvent(payload, signature, webhookSecret);
		} catch (SignatureVerificationException e) {
			log.warn("Invalid Stripe webhook signature");
			throw new PaymentFailedException("Invalid webhook signature");
		}
		String type = event.getType();
		if ("checkout.session.completed".equals(type)) {
			handleCheckoutSessionCompleted(event);
		} else if ("invoice.paid".equals(type)) {
			handleInvoicePaid(event);
		} else if ("customer.subscription.deleted".equals(type)) {
			handleSubscriptionDeleted(event);
		} else {
			log.debug("Unhandled Stripe event type: {}", type);
		}
	}

	@Transactional
	protected void handleCheckoutSessionCompleted(Event event) {
		StripeObject obj = event.getDataObjectDeserializer().getObject().orElse(null);
		if (!(obj instanceof Session session)) {
			log.warn("checkout.session.completed without Session object");
			return;
		}
		String subscriptionId = session.getSubscription();
		String customerId = session.getCustomer();
		Map<String, String> metadata = session.getMetadata();
		String userIdStr = metadata != null ? metadata.get("userId") : null;
		String planTierStr = metadata != null ? metadata.get("planTier") : null;
		if (userIdStr == null || planTierStr == null) {
			log.warn("Session metadata missing userId or planTier");
			return;
		}
		UUID userId = UUID.fromString(userIdStr);
		PlanTier planTier = PlanTier.valueOf(planTierStr);
		UUID orgId = null;
		AuthUserDto authUser = authClient.getUserById(userId);
		if (authUser != null && authUser.getOrgId() != null) {
			orgId = authUser.getOrgId();
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime periodEnd = now.plusMonths(1);
		Subscription sub = Subscription.builder()
			.orgId(orgId)
			.userId(userId)
			.stripeCustomerId(customerId)
			.stripeSubscriptionId(subscriptionId)
			.planTier(planTier)
			.status(SubscriptionStatus.ACTIVE)
			.currentPeriodStart(now)
			.currentPeriodEnd(periodEnd)
			.cancelAtPeriodEnd(false)
			.build();
		subscriptionRepository.save(sub);
		log.info("Subscription created from checkout: userId={}, planTier={}, stripeSubscriptionId={}", userId, planTier, subscriptionId);
	}

	@Transactional
	protected void handleInvoicePaid(Event event) {
		StripeObject obj = event.getDataObjectDeserializer().getObject().orElse(null);
		if (!(obj instanceof Invoice invoice)) {
			log.warn("invoice.paid event without Invoice object");
			return;
		}
		String subscriptionId = invoice.getSubscription();
		if (subscriptionId == null || subscriptionId.isBlank()) {
			log.debug("Invoice has no subscription (one-time payment?)");
			return;
		}
		Subscription sub = subscriptionRepository.findByStripeSubscriptionId(subscriptionId).orElse(null);
		if (sub == null) {
			log.warn("Subscription not found for Stripe subscription id: {}", subscriptionId);
			return;
		}
		// Update period from Stripe if needed; provision credits for the plan tier (billing key: org when present, else user)
		int credits = getCreditsForTier(sub.getPlanTier());
		creditService.provision(sub.getOrgId(), sub.getUserId(), credits, com.cognitivequantum.billing.entity.TransactionType.SUBSCRIPTION_PURCHASE, invoice.getId());
		meterRegistry.counter("payment_success_total", "plan_tier", sub.getPlanTier().name()).increment();
		log.info("invoice.paid processed: userId={}, subscriptionId={}, credits={}", sub.getUserId(), subscriptionId, credits);
	}

	@Transactional
	protected void handleSubscriptionDeleted(Event event) {
		StripeObject obj = event.getDataObjectDeserializer().getObject().orElse(null);
		if (!(obj instanceof com.stripe.model.Subscription stripeSub)) return;
		String subscriptionId = stripeSub.getId();
		if (subscriptionId == null) return;
		subscriptionRepository.findByStripeSubscriptionId(subscriptionId).ifPresent(sub -> {
			sub.setStatus(SubscriptionStatus.CANCELED);
			sub.setCancelledAt(LocalDateTime.now());
			subscriptionRepository.save(sub);
			log.info("Subscription canceled: userId={}, stripeSubscriptionId={}", sub.getUserId(), subscriptionId);
		});
	}

	private String getPriceIdForTier(PlanTier tier) {
		return switch (tier) {
			case PRO -> priceIdPro;
			case ENTERPRISE -> priceIdEnterprise;
			case FREE -> null;
		};
	}

	private int getCreditsForTier(PlanTier tier) {
		return switch (tier) {
			case PRO -> creditsPro;
			case ENTERPRISE -> creditsEnterprise;
			case FREE -> 0;
		};
	}
}
