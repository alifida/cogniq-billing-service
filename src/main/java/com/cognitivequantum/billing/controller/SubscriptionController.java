package com.cognitivequantum.billing.controller;

import com.cognitivequantum.billing.config.UserIdPrincipal;
import com.cognitivequantum.billing.dto.SubscribeRequest;
import com.cognitivequantum.billing.dto.SubscriptionDto;
import com.cognitivequantum.billing.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing/subscription")
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

	@GetMapping
	@Operation(summary = "Get current subscription", description = "Returns active subscription for the current user")
	@ApiResponse(responseCode = "200", description = "Current subscription")
	@ApiResponse(responseCode = "404", description = "No active subscription")
	public ResponseEntity<SubscriptionDto> getCurrentSubscription() {
		UUID userId = currentUserId();
		return subscriptionService.getCurrentSubscriptionOptional(userId)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.noContent().build());
	}

	@GetMapping("/history")
	@Operation(summary = "List subscription history", description = "Returns all subscriptions for the current user")
	public ResponseEntity<List<SubscriptionDto>> listSubscriptions() {
		UUID userId = currentUserId();
		return ResponseEntity.ok(subscriptionService.listSubscriptions(userId));
	}

	@PostMapping
	@Operation(summary = "Subscribe to a plan", description = "Creates or changes subscription to the given plan")
	@ApiResponse(responseCode = "200", description = "Subscription created")
	@ApiResponse(responseCode = "409", description = "Already has active subscription")
	public ResponseEntity<SubscriptionDto> subscribe(@Valid @RequestBody SubscribeRequest request) {
		UUID userId = currentUserId();
		SubscriptionDto dto = subscriptionService.subscribe(userId, request);
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/cancel")
	@Operation(summary = "Cancel at period end", description = "Sets subscription to cancel at end of current period")
	@ApiResponse(responseCode = "200", description = "Cancellation scheduled")
	public ResponseEntity<SubscriptionDto> cancelAtPeriodEnd() {
		UUID userId = currentUserId();
		SubscriptionDto dto = subscriptionService.cancelAtPeriodEnd(userId);
		return ResponseEntity.ok(dto);
	}
}
