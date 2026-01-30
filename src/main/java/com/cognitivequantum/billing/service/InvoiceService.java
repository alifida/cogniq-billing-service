package com.cognitivequantum.billing.service;

import com.cognitivequantum.billing.dto.InvoiceDto;
import com.cognitivequantum.billing.entity.Invoice;
import com.cognitivequantum.billing.exception.ResourceNotFoundException;
import com.cognitivequantum.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

	private final InvoiceRepository invoiceRepository;

	@Transactional(readOnly = true)
	public List<InvoiceDto> listInvoicesByUser(UUID userId) {
		return invoiceRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
			.stream()
			.map(this::toDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public InvoiceDto getByIdAndUser(UUID invoiceId, UUID userId) {
		Invoice inv = invoiceRepository.findByIdAndUserId(invoiceId, userId)
			.orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
		return toDto(inv);
	}

	private InvoiceDto toDto(Invoice inv) {
		String amountDisplay = inv.getAmountCents() == null ? "$0.00" : String.format("$%.2f", inv.getAmountCents() / 100.0);
		return InvoiceDto.builder()
			.id(inv.getId())
			.amountCents(inv.getAmountCents())
			.amountDisplay(amountDisplay)
			.currency(inv.getCurrency())
			.status(inv.getStatus())
			.periodStart(inv.getPeriodStart())
			.periodEnd(inv.getPeriodEnd())
			.dueDate(inv.getDueDate())
			.paidAt(inv.getPaidAt())
			.createdAt(inv.getCreatedAt())
			.build();
	}
}
