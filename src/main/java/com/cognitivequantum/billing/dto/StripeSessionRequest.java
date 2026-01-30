package com.cognitivequantum.billing.dto;

import com.cognitivequantum.billing.entity.PlanTier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create Stripe Checkout Session for subscription")
public class StripeSessionRequest {

	@NotNull(message = "Plan tier is required")
	@Schema(description = "Plan tier (FREE, PRO, ENTERPRISE)", requiredMode = Schema.RequiredMode.REQUIRED)
	private PlanTier planTier;

	@Schema(description = "URL to redirect after successful payment", example = "https://app.example.com/billing/success")
	private String successUrl;

	@Schema(description = "URL to redirect if user cancels", example = "https://app.example.com/billing/cancel")
	private String cancelUrl;
}
