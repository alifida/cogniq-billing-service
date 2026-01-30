package com.cognitivequantum.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Schema for Stripe webhook payload documentation (OpenAPI).
 * The actual request body is raw Stripe Event JSON; see https://stripe.com/docs/api/events.
 * Handled event types: checkout.session.completed, invoice.paid, customer.subscription.deleted.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
	description = "Raw Stripe Event JSON. The webhook endpoint receives the raw request body and verifies it using Stripe-Signature. " +
		"Relevant event types: checkout.session.completed (new subscription), invoice.paid (provision credits), customer.subscription.deleted (cancel).",
	example = "{\"id\":\"evt_xxx\",\"type\":\"invoice.paid\",\"data\":{\"object\":{...}}}"
)
public class WebhookDto {

	@Schema(description = "Stripe event ID (e.g. evt_xxx)", example = "evt_1ABC...")
	private String id;

	@Schema(description = "Event type: checkout.session.completed, invoice.paid, customer.subscription.deleted", example = "invoice.paid")
	private String type;

	@Schema(description = "Event data object (Stripe object)")
	private Object data;
}
