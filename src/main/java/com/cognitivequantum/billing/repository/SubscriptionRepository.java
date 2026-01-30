package com.cognitivequantum.billing.repository;

import com.cognitivequantum.billing.entity.Subscription;
import com.cognitivequantum.billing.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

	List<Subscription> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

	Optional<Subscription> findByIdAndUserId(UUID id, UUID userId);

	boolean existsByUserIdAndStatus(UUID userId, SubscriptionStatus status);
}
