package com.cognitivequantum.billing.repository;

import com.cognitivequantum.billing.entity.CreditBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface CreditBalanceRepository extends JpaRepository<CreditBalance, UUID> {

	Optional<CreditBalance> findByUserId(UUID userId);

	Optional<CreditBalance> findByOrgId(UUID orgId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM CreditBalance c WHERE c.userId = :userId")
	Optional<CreditBalance> findByUserIdForUpdate(@Param("userId") UUID userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM CreditBalance c WHERE c.orgId = :orgId")
	Optional<CreditBalance> findByOrgIdForUpdate(@Param("orgId") UUID orgId);
}
