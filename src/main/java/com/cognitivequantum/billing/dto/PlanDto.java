package com.cognitivequantum.billing.dto;

import com.cognitivequantum.billing.entity.Plan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subscription plan for display and selection")
public class PlanDto {

	@Schema(description = "Plan ID")
	private UUID id;

	@Schema(description = "Plan name", example = "Enterprise Quantum")
	private String name;

	@Schema(description = "Plan slug", example = "ENTERPRISE")
	private String slug;

	@Schema(description = "Price in cents", example = "49900")
	private Long priceAmountCents;

	@Schema(description = "Human-readable price", example = "$499 / Month")
	private String priceDisplay;

	@Schema(description = "Currency", example = "USD")
	private String currency;

	@Schema(description = "Billing interval")
	private Plan.BillingInterval interval;

	@Schema(description = "Usage limits (e.g. computeHoursLimit, teamSeatsLimit)")
	private Map<String, Integer> limits;

	@Schema(description = "Plan features/perks")
	private Map<String, Object> features;
}
