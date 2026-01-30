package com.cognitivequantum.billing.controller;

import com.cognitivequantum.billing.config.UserIdPrincipal;
import com.cognitivequantum.billing.dto.SubscribeRequest;
import com.cognitivequantum.billing.dto.SubscriptionDto;
import com.cognitivequantum.billing.util.TenantContext;
import jakarta.validation.Valid;
import com.cognitivequantum.billing.service.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Current subscription, subscribe, cancel")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	private static UUID currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof UserIdPrincipal p)) {
			throw new IllegalStateException("User not authenticated");
		}
		return p.userId();
	}

	private static UUID currentOrgId() {
		if (SecurityContextHolder.getContext().getAuthentication() != null
			&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserIdPrincipal p) {
			return p.orgId();
		}
		return TenantContext.getOrgId();
	}

	@GetMapping
	@Operation(summary = "Get current subscription", description = "Returns active subscription for the current org (or user when org not in JWT)")
	@ApiResponse(responseCode = "200", description = "Current subscription")
	@ApiResponse(responseCode = "404", description = "No active subscription")
	public ResponseEntity<SubscriptionDto> getCurrentSubscription() {
		UUID orgId = currentOrgId();
		UUID userId = currentUserId();
		return subscriptionService.getCurrentSubscriptionOptional(orgId, userId)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.noContent().build());
	}

	@PostMapping
	@Operation(summary = "Subscribe to a plan", description = "Creates a new subscription for the current org (e.g. after signup when user has no plan)")
	@ApiResponse(responseCode = "201", description = "Subscription created")
	@ApiResponse(responseCode = "400", description = "Invalid plan or org already has subscription")
	public ResponseEntity<SubscriptionDto> subscribe(@Valid @RequestBody SubscribeRequest request) {
		UUID orgId = currentOrgId();
		UUID userId = currentUserId();
		if (orgId == null) {
			throw new IllegalStateException("No organization in session; sign up with a business first");
		}
		SubscriptionDto dto = subscriptionService.createSubscription(orgId, userId, request.getPlanId());
		return ResponseEntity.status(201).body(dto);
	}

	@GetMapping("/history")
	@Operation(summary = "List subscription history", description = "Returns all subscriptions for the current org/user")
	public ResponseEntity<List<SubscriptionDto>> listSubscriptions() {
		UUID orgId = currentOrgId();
		UUID userId = currentUserId();
		return ResponseEntity.ok(subscriptionService.listSubscriptions(orgId, userId));
	}

	@PostMapping("/cancel")
	@Operation(summary = "Cancel at period end", description = "Sets subscription to cancel at end of current period")
	@ApiResponse(responseCode = "200", description = "Cancellation scheduled")
	public ResponseEntity<SubscriptionDto> cancelAtPeriodEnd() {
		UUID orgId = currentOrgId();
		UUID userId = currentUserId();
		SubscriptionDto dto = subscriptionService.cancelAtPeriodEnd(orgId, userId);
		return ResponseEntity.ok(dto);
	}
}
