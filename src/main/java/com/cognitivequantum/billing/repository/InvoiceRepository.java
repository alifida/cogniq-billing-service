package com.cognitivequantum.billing.repository;

import com.cognitivequantum.billing.entity.Invoice;
import com.cognitivequantum.billing.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

	List<Invoice> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

	Optional<Invoice> findByIdAndUserId(UUID id, UUID userId);

	List<Invoice> findAllByUserIdAndStatus(UUID userId, InvoiceStatus status);
}
