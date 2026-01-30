package com.cognitivequantum.billing.service.subscription;

import com.cognitivequantum.billing.dto.SubscriptionDto;
import com.cognitivequantum.billing.entity.PlanTier;
import com.cognitivequantum.billing.entity.Subscription;
import com.cognitivequantum.billing.entity.SubscriptionStatus;
import com.cognitivequantum.billing.exception.ResourceNotFoundException;
import com.cognitivequantum.billing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

	private final SubscriptionRepository subscriptionRepository;

	private java.util.Optional<Subscription> findActiveByBillingKey(UUID orgId, UUID userId) {
		if (orgId != null) {
			return subscriptionRepository.findByOrgIdAndStatus(orgId, SubscriptionStatus.ACTIVE)
				.or(() -> subscriptionRepository.findByOrgIdAndStatus(orgId, SubscriptionStatus.TRIALING));
		}
		return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
			.or(() -> subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.TRIALING));
	}

	@Transactional(readOnly = true)
	public SubscriptionDto getCurrentSubscription(UUID orgId, UUID userId) {
		Subscription sub = findActiveByBillingKey(orgId, userId)
			.orElseThrow(() -> new ResourceNotFoundException("No active subscription for user"));
		return toDto(sub);
	}

	@Transactional(readOnly = true)
	public java.util.Optional<SubscriptionDto> getCurrentSubscriptionOptional(UUID orgId, UUID userId) {
		return findActiveByBillingKey(orgId, userId).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public List<SubscriptionDto> listSubscriptions(UUID orgId, UUID userId) {
		List<Subscription> list = orgId != null
			? subscriptionRepository.findAllByOrgIdOrderByCreatedAtDesc(orgId)
			: subscriptionRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
		return list.stream().map(this::toDto).collect(Collectors.toList());
	}

	@Transactional
	public SubscriptionDto cancelAtPeriodEnd(UUID orgId, UUID userId) {
		Subscription sub = findActiveByBillingKey(orgId, userId)
			.orElseThrow(() -> new ResourceNotFoundException("No active subscription for user"));
		sub.setCancelAtPeriodEnd(true);
		sub = subscriptionRepository.save(sub);
		log.info("Org {} user {} set subscription to cancel at period end", orgId, userId);
		return toDto(sub);
	}

	@Transactional(readOnly = true)
	public SubscriptionDto getByIdAndBillingKey(UUID subscriptionId, UUID orgId, UUID userId) {
		Subscription sub = orgId != null
			? subscriptionRepository.findByIdAndOrgId(subscriptionId, orgId)
				.orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId))
			: subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));
		return toDto(sub);
	}

	public Subscription getActiveSubscriptionEntity(UUID orgId, UUID userId) {
		return findActiveByBillingKey(orgId, userId).orElse(null);
	}

	private SubscriptionDto toDto(Subscription sub) {
		PlanTier tier = sub.getPlanTier();
		String planName = tier == null ? "Unknown" : switch (tier) {
			case FREE -> "Free";
			case PRO -> "Pro";
			case ENTERPRISE -> "Enterprise Quantum";
		};
		String planSlug = tier == null ? "" : tier.name();
		String priceDisplay = tier == null ? "" : switch (tier) {
			case FREE -> "Free";
			case PRO -> "$99 / Month";
			case ENTERPRISE -> "$499 / Month";
		};
		return SubscriptionDto.builder()
			.id(sub.getId())
			.planTier(tier)
			.planName(planName)
			.planSlug(planSlug)
			.priceDisplay(priceDisplay)
			.status(sub.getStatus())
			.currentPeriodStart(sub.getCurrentPeriodStart())
			.currentPeriodEnd(sub.getCurrentPeriodEnd())
			.cancelAtPeriodEnd(sub.getCancelAtPeriodEnd())
			.cancelledAt(sub.getCancelledAt())
			.stripeCustomerId(sub.getStripeCustomerId())
			.stripeSubscriptionId(sub.getStripeSubscriptionId())
			.build();
	}
}
