package com.cognitivequantum.billing.repository;

import com.cognitivequantum.billing.entity.Transaction;
import com.cognitivequantum.billing.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

	List<Transaction> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

	Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

	List<Transaction> findAllByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, TransactionType type);
}
