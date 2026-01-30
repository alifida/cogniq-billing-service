package com.cognitivequantum.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
	@Index(name = "idx_transaction_user_id", columnList = "user_id"),
	@Index(name = "idx_transaction_type", columnList = "type"),
	@Index(name = "idx_transaction_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(name = "currency", length = 3)
	@Builder.Default
	private String currency = "USD";

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private TransactionType type;

	@Column(name = "correlation_id", length = 256)
	private String correlationId;

	@Column(name = "stripe_invoice_id", length = 256)
	private String stripeInvoiceId;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
