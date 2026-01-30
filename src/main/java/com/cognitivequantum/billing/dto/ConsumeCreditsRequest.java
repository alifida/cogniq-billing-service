package com.cognitivequantum.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to consume credits (e.g. when Orchestrator starts a training job)")
public class ConsumeCreditsRequest {

	@NotNull(message = "Amount is required")
	@Min(value = 1, message = "Amount must be at least 1")
	@Schema(description = "Number of credits to consume", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
	private Integer amount;

	@Schema(description = "Correlation ID to trace back to ML Training Job (X-Correlation-Id or this field)")
	private String correlationId;
}
