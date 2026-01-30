package com.cognitivequantum.billing.client;

import com.cognitivequantum.billing.client.dto.AuthUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client to Auth service for user data (e.g. email for Stripe customer).
 * Auth service should expose GET /api/auth/internal/users/{id} or similar for internal use.
 */
@FeignClient(name = "cogniq-auth-service", path = "/api/auth", fallback = AuthClientFallback.class)
public interface AuthClient {

	@GetMapping("/internal/users/{userId}")
	AuthUserDto getUserById(@PathVariable("userId") UUID userId);
}
