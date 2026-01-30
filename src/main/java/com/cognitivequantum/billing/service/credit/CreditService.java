package com.cognitivequantum.billing.service.credit;

import com.cognitivequantum.billing.dto.BillingSummaryDto;
import com.cognitivequantum.billing.dto.CreditBalanceDto;
import com.cognitivequantum.billing.entity.CreditBalance;
import com.cognitivequantum.billing.entity.Transaction;
import com.cognitivequantum.billing.entity.TransactionType;
import com.cognitivequantum.billing.exception.InsufficientCreditsException;
import com.cognitivequantum.billing.repository.CreditBalanceRepository;
import com.cognitivequantum.billing.repository.TransactionRepository;
import com.cognitivequantum.billing.util.AuditLogUtil;
import io.micrometer.core.instrument.Counter;
import java.util.Optional;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService {

	private final CreditBalanceRepository creditBalanceRepository;
	private final TransactionRepository transactionRepository;
	private final MeterRegistry meterRegistry;

	/** Resolve balance by org (billing entity) when orgId present, else by user (legacy). */
	private Optional<CreditBalance> findBalance(UUID orgId, UUID userId) {
		if (orgId != null) return creditBalanceRepository.findByOrgId(orgId);
		return creditBalanceRepository.findByUserId(userId);
	}

	private CreditBalance findOrCreateBalanceForUpdate(UUID orgId, UUID userId) {
		if (orgId != null) {
			return creditBalanceRepository.findByOrgIdForUpdate(orgId)
				.orElseGet(() -> creditBalanceRepository.save(CreditBalance.builder().orgId(orgId).totalCredits(0).usedCredits(0).build()));
		}
		return creditBalanceRepository.findByUserIdForUpdate(userId)
			.orElseGet(() -> creditBalanceRepository.save(CreditBalance.builder().userId(userId).totalCredits(0).usedCredits(0).build()));
	}

	@Transactional(readOnly = true)
	public CreditBalanceDto getBalance(UUID orgId, UUID userId) {
		CreditBalance balance = findBalance(orgId, userId)
			.orElseGet(() -> CreditBalance.builder().orgId(orgId).userId(userId).totalCredits(0).usedCredits(0).build());
		return toDto(balance);
	}

	/**
	 * Consume credits (e.g. when Orchestrator starts a training job).
	 * Billing key: orgId when present (multi-tenant), else userId (legacy).
	 */
	@Transactional
	public CreditBalanceDto consume(UUID orgId, UUID userId, int amount, String correlationId) {
		String corrId = correlationId != null && !correlationId.isBlank() ? correlationId : AuditLogUtil.getCorrelationId();
		CreditBalance balance = findOrCreateBalanceForUpdate(orgId, userId);
		int available = balance.getAvailableCredits();
		if (available < amount) {
			meterRegistry.counter("payment_failure_total", "reason", "insufficient_credits").increment();
			log.warn("Insufficient credits: orgId={}, userId={}, available={}, requested={}, correlationId={}", orgId, userId, available, amount, corrId);
			throw new InsufficientCreditsException("Insufficient credits. Available: " + available + ", required: " + amount);
		}
		balance.setUsedCredits(balance.getUsedCredits() + amount);
		creditBalanceRepository.save(balance);
		Transaction tx = Transaction.builder()
			.userId(userId)
			.amount(BigDecimal.valueOf(-amount))
			.currency("USD")
			.type(TransactionType.CONSUME)
			.correlationId(corrId)
			.build();
		transactionRepository.save(tx);
		log.info("Credits consumed: orgId={}, userId={}, amount={}, correlationId={}, availableAfter={}", orgId, userId, amount, corrId, balance.getAvailableCredits());
		return toDto(balance);
	}

	/** Provision credits (e.g. on invoice.paid webhook). Billing key: orgId when present, else userId. */
	@Transactional
	public void provision(UUID orgId, UUID userId, int credits, TransactionType type, String stripeInvoiceId) {
		CreditBalance balance = findOrCreateBalanceForUpdate(orgId, userId);
		balance.setTotalCredits(balance.getTotalCredits() + credits);
		creditBalanceRepository.save(balance);
		Transaction tx = Transaction.builder()
			.userId(userId)
			.amount(BigDecimal.valueOf(credits))
			.currency("USD")
			.type(type)
			.stripeInvoiceId(stripeInvoiceId)
			.build();
		transactionRepository.save(tx);
		log.info("Credits provisioned: orgId={}, userId={}, credits={}, type={}", orgId, userId, credits, type);
	}

	@Transactional(readOnly = true)
	public List<BillingSummaryDto.TransactionDto> getRecentTransactions(UUID orgId, UUID userId, int limit) {
		return transactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
			.limit(limit)
			.map(this::toTransactionDto)
			.collect(Collectors.toList());
	}

	private CreditBalanceDto toDto(CreditBalance b) {
		return CreditBalanceDto.builder()
			.totalCredits(b.getTotalCredits())
			.usedCredits(b.getUsedCredits())
			.availableCredits(b.getAvailableCredits())
			.build();
	}

	private BillingSummaryDto.TransactionDto toTransactionDto(Transaction t) {
		return BillingSummaryDto.TransactionDto.builder()
			.id(t.getId())
			.amountDisplay(com.cognitivequantum.billing.util.CurrencyUtil.formatUsd(t.getAmount()))
			.currency(t.getCurrency())
			.type(t.getType().name())
			.correlationId(t.getCorrelationId())
			.createdAt(t.getCreatedAt())
			.build();
	}
}
