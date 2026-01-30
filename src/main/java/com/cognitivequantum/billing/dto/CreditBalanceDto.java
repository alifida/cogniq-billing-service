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
@Schema(description = "Credit balance for ML training")
public class CreditBalanceDto {

	@Schema(description = "Total credits allocated")
	private int totalCredits;

	@Schema(description = "Credits consumed to date")
	private int usedCredits;

	@Schema(description = "Available credits (total - used)")
	private int availableCredits;
}
