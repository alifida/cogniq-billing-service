package com.cognitivequantum.billing.controller;

import com.cognitivequantum.billing.dto.WebhookDto;
import com.cognitivequantum.billing.service.payment.StripePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Stripe webhooks; public endpoint with Stripe Signature Verification.
 */
@RestController
@RequestMapping("/api/billing/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Stripe webhook (invoice.paid, checkout.session.completed); uses Stripe Signature Verification")
public class WebhookController {

	private final StripePaymentService stripePaymentService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
		summary = "Stripe webhook",
		description = "Receives Stripe events as raw JSON body. Verifies Stripe-Signature header. " +
			"Handled events: checkout.session.completed (new subscription), invoice.paid (provision credits), customer.subscription.deleted (cancel). " +
			"Payload: raw Stripe Event JSON (see https://stripe.com/docs/api/events). Documented by WebhookDto schema."
	)
	public ResponseEntity<Void> handleWebhook(
		@RequestBody @Schema(description = "Raw Stripe Event JSON", implementation = WebhookDto.class, example = "{\"id\":\"evt_xxx\",\"type\":\"invoice.paid\",\"data\":{\"object\":{...}}}") String payload,
		@RequestHeader(value = "Stripe-Signature", required = false) String signature
	) {
		if (signature == null || signature.isBlank()) {
			log.warn("Webhook received without Stripe-Signature");
			return ResponseEntity.badRequest().build();
		}
		try {
			stripePaymentService.handleWebhook(payload, signature);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("Webhook handling failed: {}", e.getMessage());
			return ResponseEntity.badRequest().build();
		}
	}
}
