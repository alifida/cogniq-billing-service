package com.cognitivequantum.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices", indexes = {
	@Index(name = "idx_invoice_user_id", columnList = "user_id"),
	@Index(name = "idx_invoice_subscription_id", columnList = "subscription_id"),
	@Index(name = "idx_invoice_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "subscription_id", nullable = false)
	private UUID subscriptionId;

	@Column(name = "amount_cents", nullable = false)
	private Long amountCents;

	@Column(length = 3)
	@Builder.Default
	private String currency = "USD";

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private InvoiceStatus status;

	@Column(name = "period_start", nullable = false)
	private LocalDate periodStart;

	@Column(name = "period_end", nullable = false)
	private LocalDate periodEnd;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	@Column(name = "external_id", length = 256)
	private String externalId;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
