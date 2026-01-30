package com.cognitivequantum.billing.controller;

import com.cognitivequantum.billing.config.UserIdPrincipal;
import com.cognitivequantum.billing.dto.InvoiceDto;
import com.cognitivequantum.billing.service.InvoiceService;
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
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Billing history and invoice details")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

	private final InvoiceService invoiceService;

	private static UUID currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof UserIdPrincipal p)) {
			throw new IllegalStateException("User not authenticated");
		}
		return p.userId();
	}

	@GetMapping
	@Operation(summary = "List invoices", description = "Returns billing history for the current user")
	public ResponseEntity<List<InvoiceDto>> listInvoices() {
		UUID userId = currentUserId();
		return ResponseEntity.ok(invoiceService.listInvoicesByUser(userId));
	}

	@GetMapping("/{invoiceId}")
	@Operation(summary = "Get invoice", description = "Returns invoice by ID")
	@ApiResponse(responseCode = "200", description = "Invoice details")
	@ApiResponse(responseCode = "404", description = "Invoice not found")
	public ResponseEntity<InvoiceDto> getInvoice(@PathVariable UUID invoiceId) {
		UUID userId = currentUserId();
		return ResponseEntity.ok(invoiceService.getByIdAndUser(invoiceId, userId));
	}
}
