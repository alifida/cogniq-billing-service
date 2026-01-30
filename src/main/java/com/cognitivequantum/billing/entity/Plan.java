package com.cognitivequantum.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "plans", indexes = {
	@Index(name = "idx_plan_slug", columnList = "slug", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(nullable = false, unique = true, length = 64)
	private String slug;

	@Column(name = "price_amount_cents")
	private Long priceAmountCents;

	@Column(length = 3)
	@Builder.Default
	private String currency = "USD";

	@Enumerated(EnumType.STRING)
	@Column(name = "billing_interval", nullable = false, length = 32)
	@Builder.Default
	private BillingInterval interval = BillingInterval.MONTHLY;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "limits")
	private Map<String, Integer> limits;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "features")
	private Map<String, Object> features;

	@Column(nullable = false)
	@Builder.Default
	private Boolean active = true;

	@Column(name = "sort_order")
	@Builder.Default
	private Integer sortOrder = 0;

	public enum BillingInterval {
		MONTHLY,
		YEARLY
	}
}
