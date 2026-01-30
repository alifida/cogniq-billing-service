package com.cognitivequantum.billing.controller;

import com.cognitivequantum.billing.config.UserIdPrincipal;
import com.cognitivequantum.billing.dto.UsageSummaryDto;
import com.cognitivequantum.billing.service.UsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/usage")
@RequiredArgsConstructor
@Tag(name = "Usage", description = "Current period usage and limits")
@SecurityRequirement(name = "bearerAuth")
public class UsageController {

	private final UsageService usageService;

	private static UUID currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof UserIdPrincipal p)) {
			throw new IllegalStateException("User not authenticated");
		}
		return p.userId();
	}

	@GetMapping
	@Operation(summary = "Get usage summary", description = "Returns used quantities and limits for current period (e.g. compute hours, team seats)")
	public ResponseEntity<UsageSummaryDto> getUsage() {
		UUID orgId = com.cognitivequantum.billing.util.TenantContext.getOrgId();
		UUID userId = currentUserId();
		return ResponseEntity.ok(usageService.getUsageSummary(orgId, userId));
	}
}
