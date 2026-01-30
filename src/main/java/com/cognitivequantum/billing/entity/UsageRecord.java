package com.cognitivequantum.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usage_records", indexes = {
	@Index(name = "idx_usage_user_id", columnList = "user_id"),
	@Index(name = "idx_usage_subscription_id", columnList = "subscription_id"),
	@Index(name = "idx_usage_type_period", columnList = "usage_type, period_start")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "subscription_id")
	private UUID subscriptionId;

	@Enumerated(EnumType.STRING)
	@Column(name = "usage_type", nullable = false)
	private UsageType usageType;

	@Column(nullable = false)
	private Long quantity;

	@Column(name = "period_start", nullable = false)
	private LocalDateTime periodStart;

	@Column(name = "period_end", nullable = false)
	private LocalDateTime periodEnd;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
