package com.cognitivequantum.billing.controller;

import com.cognitivequantum.billing.service.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Internal endpoints for other services (e.g. Auth) to check subscription limits.
 * Not exposed via gateway; service-to-service only.
 */
@RestController
@RequestMapping("/internal/org")
@RequiredArgsConstructor
@Hidden
public class InternalSubscriptionController {

	private final SubscriptionService subscriptionService;

	@GetMapping("/{orgId}/subscription-limits")
	public ResponseEntity<Map<String, Integer>> getSubscriptionLimits(@PathVariable UUID orgId) {
		int maxUsers = subscriptionService.getMaxUsersForOrg(orgId);
		return ResponseEntity.ok(Map.of("maxUsers", maxUsers));
	}
}
