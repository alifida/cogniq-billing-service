package com.cognitivequantum.billing.dto;

import com.cognitivequantum.billing.entity.PlanTier;
import com.cognitivequantum.billing.entity.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Billing summary: subscription, credits, recent transactions")
public class BillingSummaryDto {

	@Schema(description = "Current subscription (null if none)")
	private SubscriptionSummaryDto subscription;

	@Schema(description = "Credit balance")
	private CreditBalanceDto credits;

	@Schema(description = "Recent transactions (audit log)")
	private List<TransactionDto> recentTransactions;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SubscriptionSummaryDto {
		private UUID id;
		private PlanTier planTier;
		private SubscriptionStatus status;
		private LocalDateTime currentPeriodEnd;
		private String stripeCustomerId;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CreditBalanceDto {
		private int totalCredits;
		private int usedCredits;
		private int availableCredits;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TransactionDto {
		private UUID id;
		private String amountDisplay;
		private String currency;
		private String type;
		private String correlationId;
		private LocalDateTime createdAt;
	}
}
