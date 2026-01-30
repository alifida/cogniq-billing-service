package com.cognitivequantum.billing.controller;

import com.cognitivequantum.billing.config.UserIdPrincipal;
import com.cognitivequantum.billing.dto.ConsumeCreditsRequest;
import com.cognitivequantum.billing.util.TenantContext;
import com.cognitivequantum.billing.dto.CreditBalanceDto;
import com.cognitivequantum.billing.service.credit.CreditService;
import com.cognitivequantum.billing.util.AuditLogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/billing/credits")
@RequiredArgsConstructor
@Tag(name = "Credits", description = "Credit balance and consume (e.g. Orchestrator calls consume when starting job)")
@SecurityRequirement(name = "bearerAuth")
public class CreditsController {

	private final CreditService creditService;

	private static UUID currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof UserIdPrincipal p)) {
			throw new IllegalStateException("User not authenticated");
		}
		return p.userId();
	}

	private static UUID currentOrgId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UserIdPrincipal p) {
			return p.orgId();
		}
		return TenantContext.getOrgId();
	}

	@GetMapping
	@Operation(summary = "Get credit balance", description = "Returns total, used, and available credits for the current org (or user when org not in JWT)")
	public ResponseEntity<CreditBalanceDto> getBalance() {
		UUID orgId = currentOrgId();
		UUID userId = currentUserId();
		return ResponseEntity.ok(creditService.getBalance(orgId, userId));
	}

	@PostMapping("/consume")
	@Operation(
		summary = "Consume credits",
		description = "Decrements credits when Orchestrator starts a training job. Returns 200 OK if credits available, 402 Payment Required if 0. " +
			"Every credit deduction is logged with a Correlation ID (X-Correlation-Id or request.correlationId) to trace back to the ML Training Job."
	)
	@ApiResponse(responseCode = "200", description = "Credits consumed")
	@ApiResponse(responseCode = "402", description = "Insufficient credits (Payment Required)")
	public ResponseEntity<CreditBalanceDto> consume(
		@Valid @RequestBody ConsumeCreditsRequest request,
		HttpServletRequest httpRequest
	) {
		UUID orgId = currentOrgId();
		UUID userId = currentUserId();
		String correlationId = AuditLogUtil.getOrCreateCorrelationId(
			request.getCorrelationId() != null ? request.getCorrelationId() : httpRequest.getHeader(AuditLogUtil.CORRELATION_ID_HEADER));
		CreditBalanceDto dto = creditService.consume(orgId, userId, request.getAmount(), correlationId);
		return ResponseEntity.ok(dto);
	}
}
