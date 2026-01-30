package com.cognitivequantum.billing.repository;

import com.cognitivequantum.billing.entity.Subscription;
import com.cognitivequantum.billing.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

	Optional<Subscription> findByOrgIdAndStatus(UUID orgId, SubscriptionStatus status);

	List<Subscription> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

	List<Subscription> findAllByOrgIdOrderByCreatedAtDesc(UUID orgId);

	Optional<Subscription> findByIdAndUserId(UUID id, UUID userId);

	Optional<Subscription> findByIdAndOrgId(UUID id, UUID orgId);

	Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

	boolean existsByUserIdAndStatus(UUID userId, SubscriptionStatus status);

	boolean existsByOrgIdAndStatus(UUID orgId, SubscriptionStatus status);
}
