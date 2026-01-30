package com.cognitivequantum.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions", indexes = {
	@Index(name = "idx_subscription_user_id", columnList = "user_id"),
	@Index(name = "idx_subscription_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "plan_id", nullable = false)
	private UUID planId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

	@Column(name = "current_period_start", nullable = false)
	private LocalDateTime currentPeriodStart;

	@Column(name = "current_period_end", nullable = false)
	private LocalDateTime currentPeriodEnd;

	@Column(name = "cancel_at_period_end")
	@Builder.Default
	private Boolean cancelAtPeriodEnd = false;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	@Column(name = "external_id", length = 256)
	private String externalId;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
