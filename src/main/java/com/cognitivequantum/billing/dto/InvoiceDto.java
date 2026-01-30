package com.cognitivequantum.billing.dto;

import com.cognitivequantum.billing.entity.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Invoice for billing history")
public class InvoiceDto {

	@Schema(description = "Invoice ID")
	private UUID id;

	@Schema(description = "Amount in cents")
	private Long amountCents;

	@Schema(description = "Human-readable amount", example = "$499.00")
	private String amountDisplay;

	@Schema(description = "Currency", example = "USD")
	private String currency;

	@Schema(description = "Invoice status")
	private InvoiceStatus status;

	@Schema(description = "Billing period start")
	private LocalDate periodStart;

	@Schema(description = "Billing period end")
	private LocalDate periodEnd;

	@Schema(description = "Due date")
	private LocalDate dueDate;

	@Schema(description = "When invoice was paid")
	private LocalDateTime paidAt;

	@Schema(description = "Created at")
	private LocalDateTime createdAt;
}
