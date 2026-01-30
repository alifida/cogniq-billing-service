package com.cognitivequantum.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stripe Checkout Session URL for redirect")
public class CheckoutSessionResponse {

	@Schema(description = "URL to redirect user to Stripe hosted checkout page")
	private String checkoutUrl;

	@Schema(description = "Stripe session ID")
	private String sessionId;
}
