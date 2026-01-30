package com.cognitivequantum.billing.repository;

import com.cognitivequantum.billing.entity.UsageRecord;
import com.cognitivequantum.billing.entity.UsageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {

	List<UsageRecord> findAllByUserIdAndPeriodStartAfterAndPeriodEndBeforeOrderByUsageType(
		UUID userId, LocalDateTime periodStart, LocalDateTime periodEnd);

	@Query("SELECT COALESCE(SUM(u.quantity), 0) FROM UsageRecord u WHERE u.userId = :userId AND u.usageType = :usageType AND u.periodStart >= :periodStart AND u.periodEnd <= :periodEnd")
	Long sumQuantityByUserIdAndUsageTypeAndPeriod(
		@Param("userId") UUID userId,
		@Param("usageType") UsageType usageType,
		@Param("periodStart") LocalDateTime periodStart,
		@Param("periodEnd") LocalDateTime periodEnd);

	Optional<UsageRecord> findFirstByUserIdAndUsageTypeAndPeriodStartAfterAndPeriodEndBeforeOrderByCreatedAtDesc(
		UUID userId, UsageType usageType, LocalDateTime periodStart, LocalDateTime periodEnd);
}
