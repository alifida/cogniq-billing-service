package com.cognitivequantum.billing.service;

import com.cognitivequantum.billing.dto.SubscribeRequest;
import com.cognitivequantum.billing.dto.SubscriptionDto;
import com.cognitivequantum.billing.entity.Plan;
import com.cognitivequantum.billing.entity.Subscription;
import com.cognitivequantum.billing.entity.SubscriptionStatus;
import com.cognitivequantum.billing.exception.ConflictException;
import com.cognitivequantum.billing.exception.ResourceNotFoundException;
import com.cognitivequantum.billing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

	private final SubscriptionRepository subscriptionRepository;
	private final PlanService planService;

	@Transactional(readOnly = true)
	public SubscriptionDto getCurrentSubscription(UUID userId) {
		Subscription sub = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
			.or(() -> subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.TRIALING))
			.orElseThrow(() -> new ResourceNotFoundException("No active subscription for user"));
		Plan plan = planService.getPlanEntity(sub.getPlanId());
		return toDto(sub, plan);
	}

	@Transactional(readOnly = true)
	public java.util.Optional<SubscriptionDto> getCurrentSubscriptionOptional(UUID userId) {
		return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
			.or(() -> subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.TRIALING))
			.map(sub -> {
				Plan plan = planService.getPlanEntity(sub.getPlanId());
				return toDto(sub, plan);
			});
	}

	@Transactional(readOnly = true)
	public List<SubscriptionDto> listSubscriptions(UUID userId) {
		return subscriptionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
			.stream()
			.map(sub -> toDto(sub, planService.getPlanEntity(sub.getPlanId())))
			.collect(Collectors.toList());
	}

	@Transactional
	public SubscriptionDto subscribe(UUID userId, SubscribeRequest request) {
		Plan plan = planService.getPlanEntity(request.getPlanId());
		subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
			.ifPresent(existing -> {
				throw new ConflictException("User already has an active subscription; change plan or cancel first");
			});
		YearMonth now = YearMonth.now();
		LocalDateTime periodStart = now.atDay(1).atStartOfDay();
		LocalDateTime periodEnd = now.plusMonths(1).atDay(1).atStartOfDay();
		Subscription sub = Subscription.builder()
			.userId(userId)
			.planId(plan.getId())
			.status(SubscriptionStatus.ACTIVE)
			.currentPeriodStart(periodStart)
			.currentPeriodEnd(periodEnd)
			.cancelAtPeriodEnd(false)
			.build();
		sub = subscriptionRepository.save(sub);
		log.info("User {} subscribed to plan {}", userId, plan.getSlug());
		return toDto(sub, plan);
	}

	@Transactional
	public SubscriptionDto cancelAtPeriodEnd(UUID userId) {
		Subscription sub = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
			.orElseThrow(() -> new ResourceNotFoundException("No active subscription for user"));
		sub.setCancelAtPeriodEnd(true);
		sub = subscriptionRepository.save(sub);
		Plan plan = planService.getPlanEntity(sub.getPlanId());
		log.info("User {} set subscription to cancel at period end", userId);
		return toDto(sub, plan);
	}

	@Transactional(readOnly = true)
	public SubscriptionDto getByIdAndUser(UUID subscriptionId, UUID userId) {
		Subscription sub = subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
			.orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));
		Plan plan = planService.getPlanEntity(sub.getPlanId());
		return toDto(sub, plan);
	}

	Subscription getActiveSubscriptionEntity(UUID userId) {
		return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
			.or(() -> subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.TRIALING))
			.orElse(null);
	}

	private SubscriptionDto toDto(Subscription sub, Plan plan) {
		String priceDisplay = plan.getPriceAmountCents() == null || plan.getPriceAmountCents() == 0
			? "Free"
			: String.format("$%.2f / %s", plan.getPriceAmountCents() / 100.0, plan.getInterval() == Plan.BillingInterval.YEARLY ? "Year" : "Month");
		return SubscriptionDto.builder()
			.id(sub.getId())
			.planId(plan.getId())
			.planName(plan.getName())
			.planSlug(plan.getSlug())
			.priceDisplay(priceDisplay)
			.status(sub.getStatus())
			.currentPeriodStart(sub.getCurrentPeriodStart())
			.currentPeriodEnd(sub.getCurrentPeriodEnd())
			.cancelAtPeriodEnd(sub.getCancelAtPeriodEnd())
			.cancelledAt(sub.getCancelledAt())
			.build();
	}
}
