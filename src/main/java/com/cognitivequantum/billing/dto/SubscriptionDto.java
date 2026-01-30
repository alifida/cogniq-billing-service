package com.cognitivequantum.billing.dto;

import com.cognitivequantum.billing.entity.PlanTier;
import com.cognitivequantum.billing.entity.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current user subscription with plan summary")
public class SubscriptionDto {

	@Schema(description = "Subscription ID")
	private UUID id;

	@Schema(description = "Plan tier (FREE, PRO, ENTERPRISE)")
	private PlanTier planTier;

	@Schema(description = "Plan name", example = "Enterprise Quantum")
	private String planName;

	@Schema(description = "Plan slug", example = "ENTERPRISE")
	private String planSlug;

	@Schema(description = "Price display", example = "$499 / Month")
	private String priceDisplay;

	@Schema(description = "Subscription status")
	private SubscriptionStatus status;

	@Schema(description = "Current period start")
	private LocalDateTime currentPeriodStart;

	@Schema(description = "Current period end (next invoice date)")
	private LocalDateTime currentPeriodEnd;

	@Schema(description = "Whether subscription will cancel at period end")
	private Boolean cancelAtPeriodEnd;

	@Schema(description = "When subscription was cancelled if applicable")
	private LocalDateTime cancelledAt;

	@Schema(description = "Stripe customer ID")
	private String stripeCustomerId;

	@Schema(description = "Stripe subscription ID")
	private String stripeSubscriptionId;

	@Schema(description = "Max users (team seats) allowed by plan")
	private Integer maxUsers;
}
