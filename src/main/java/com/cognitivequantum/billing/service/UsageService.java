package com.cognitivequantum.billing.service;

import com.cognitivequantum.billing.dto.UsageSummaryDto;
import com.cognitivequantum.billing.entity.Plan;
import com.cognitivequantum.billing.entity.Subscription;
import com.cognitivequantum.billing.entity.UsageType;
import com.cognitivequantum.billing.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsageService {

	private final UsageRecordRepository usageRecordRepository;
	private final SubscriptionService subscriptionService;
	private final PlanService planService;

	@Transactional(readOnly = true)
	public UsageSummaryDto getUsageSummary(UUID userId) {
		Subscription sub = subscriptionService.getActiveSubscriptionEntity(userId);
		YearMonth now = YearMonth.now();
		LocalDateTime periodStart = now.atDay(1).atStartOfDay();
		LocalDateTime periodEnd = now.plusMonths(1).atDay(1).atStartOfDay();

		Map<UsageType, Long> used = new EnumMap<>(UsageType.class);
		Map<UsageType, Integer> limits = new EnumMap<>(UsageType.class);
		Map<UsageType, Integer> percentUsed = new EnumMap<>(UsageType.class);

		for (UsageType type : UsageType.values()) {
			Long sum = usageRecordRepository.sumQuantityByUserIdAndUsageTypeAndPeriod(userId, type, periodStart, periodEnd);
			used.put(type, sum != null ? sum : 0L);
			int limit = 0;
			if (sub != null) {
				Plan plan = planService.getPlanEntity(sub.getPlanId());
				if (plan.getLimits() != null && plan.getLimits().containsKey(type.name().toLowerCase())) {
					limit = plan.getLimits().get(type.name().toLowerCase());
				}
			}
			limits.put(type, limit);
			int pct = limit > 0 ? (int) Math.min(100, (used.get(type) * 100) / limit) : 0;
			percentUsed.put(type, pct);
		}
		return UsageSummaryDto.builder()
			.used(used)
			.limits(limits)
			.percentUsed(percentUsed)
			.build();
	}

	@Transactional
	public void recordUsage(UUID userId, UUID subscriptionId, UsageType usageType, long quantity) {
		YearMonth now = YearMonth.now();
		LocalDateTime periodStart = now.atDay(1).atStartOfDay();
		LocalDateTime periodEnd = now.plusMonths(1).atDay(1).atStartOfDay();
		com.cognitivequantum.billing.entity.UsageRecord record = com.cognitivequantum.billing.entity.UsageRecord.builder()
			.userId(userId)
			.subscriptionId(subscriptionId)
			.usageType(usageType)
			.quantity(quantity)
			.periodStart(periodStart)
			.periodEnd(periodEnd)
			.build();
		usageRecordRepository.save(record);
	}
}
