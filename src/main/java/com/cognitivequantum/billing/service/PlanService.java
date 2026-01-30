package com.cognitivequantum.billing.service;

import com.cognitivequantum.billing.dto.PlanDto;
import com.cognitivequantum.billing.entity.Plan;
import com.cognitivequantum.billing.exception.ResourceNotFoundException;
import com.cognitivequantum.billing.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepository;

	public List<PlanDto> listActivePlans() {
		return planRepository.findAllByActiveTrueOrderBySortOrderAsc()
			.stream()
			.map(this::toDto)
			.collect(Collectors.toList());
	}

	public PlanDto getPlan(UUID planId) {
		Plan plan = planRepository.findById(planId)
			.orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
		return toDto(plan);
	}

	public Plan getPlanEntity(UUID planId) {
		return planRepository.findById(planId)
			.orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
	}

	private PlanDto toDto(Plan p) {
		String priceDisplay = p.getPriceAmountCents() == null || p.getPriceAmountCents() == 0
			? "Free"
			: String.format("$%.2f / %s", p.getPriceAmountCents() / 100.0, p.getInterval() == Plan.BillingInterval.YEARLY ? "Year" : "Month");
		return PlanDto.builder()
			.id(p.getId())
			.name(p.getName())
			.slug(p.getSlug())
			.priceAmountCents(p.getPriceAmountCents())
			.priceDisplay(priceDisplay)
			.currency(p.getCurrency())
			.interval(p.getInterval())
			.limits(p.getLimits())
			.features(p.getFeatures())
			.build();
	}
}
