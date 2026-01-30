package com.cognitivequantum.billing.repository;

import com.cognitivequantum.billing.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

	List<Plan> findAllByActiveTrueOrderBySortOrderAsc();

	Optional<Plan> findBySlug(String slug);

	boolean existsBySlug(String slug);
}
