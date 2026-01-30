package com.cognitivequantum.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "credit_balances", indexes = {
	@Index(name = "idx_credit_balance_org_id", columnList = "org_id", unique = true),
	@Index(name = "idx_credit_balance_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditBalance {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "org_id")
	private UUID orgId;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "total_credits", nullable = false)
	@Builder.Default
	private Integer totalCredits = 0;

	@Column(name = "used_credits", nullable = false)
	@Builder.Default
	private Integer usedCredits = 0;

	/** Available credits = totalCredits - usedCredits */
	public int getAvailableCredits() {
		return Math.max(0, totalCredits - usedCredits);
	}
}
