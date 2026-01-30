package com.cognitivequantum.billing.controller;

import com.cognitivequantum.billing.dto.PlanDto;
import com.cognitivequantum.billing.service.subscription.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "List and get subscription plans")
public class PlanController {

	private final PlanService planService;

	@GetMapping
	@Operation(summary = "List plans", description = "Returns all active plans for upgrade/selection")
	public ResponseEntity<List<PlanDto>> listPlans() {
		return ResponseEntity.ok(planService.listActivePlans());
	}

	@GetMapping("/{planId}")
	@Operation(summary = "Get plan", description = "Returns plan by ID")
	public ResponseEntity<PlanDto> getPlan(@PathVariable UUID planId) {
		return ResponseEntity.ok(planService.getPlan(planId));
	}
}
