package com.cognitivequantum.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create or change subscription")
public class SubscribeRequest {

	@NotNull(message = "Plan ID is required")
	@Schema(description = "Plan ID to subscribe to", requiredMode = Schema.RequiredMode.REQUIRED)
	private UUID planId;
}
