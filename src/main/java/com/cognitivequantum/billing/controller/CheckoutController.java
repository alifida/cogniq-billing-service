package com.cognitivequantum.billing.controller;

import com.cognitivequantum.billing.config.UserIdPrincipal;
import com.cognitivequantum.billing.dto.CheckoutSessionResponse;
import com.cognitivequantum.billing.dto.StripeSessionRequest;
import com.cognitivequantum.billing.service.payment.StripePaymentService;
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

import java.util.UUID;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Stripe Checkout Session for subscription")
@SecurityRequirement(name = "bearerAuth")
public class CheckoutController {

	private final StripePaymentService stripePaymentService;

	private static UUID currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof UserIdPrincipal p)) {
			throw new IllegalStateException("User not authenticated");
		}
		return p.userId();
	}

	@PostMapping("/session")
	@Operation(summary = "Create Checkout Session", description = "Creates Stripe Checkout Session and returns URL to redirect user to Stripe hosted page")
	@ApiResponse(responseCode = "200", description = "Checkout URL returned")
	@ApiResponse(responseCode = "400", description = "Payment failed or Stripe down (Payment System Maintenance)")
	public ResponseEntity<CheckoutSessionResponse> createSession(@Valid @RequestBody StripeSessionRequest request) {
		UUID userId = currentUserId();
		String successUrl = request.getSuccessUrl() != null ? request.getSuccessUrl() : "https://example.com/billing/success";
		String cancelUrl = request.getCancelUrl() != null ? request.getCancelUrl() : "https://example.com/billing/cancel";
		String url = stripePaymentService.createCheckoutSession(userId, request.getPlanTier(), successUrl, cancelUrl);
		return ResponseEntity.ok(CheckoutSessionResponse.builder()
			.checkoutUrl(url)
			.sessionId(null)
			.build());
	}
}
