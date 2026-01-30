package com.cognitivequantum.billing.util;

import java.util.UUID;

/**
 * Thread-local tenant context from JWT (org_id, branch_id, userId).
 * Billing is keyed by org_id: Subscription and CreditBalance belong to the Organization.
 */
public final class TenantContext {

	private static final ThreadLocal<UUID> ORG_ID = new ThreadLocal<>();
	private static final ThreadLocal<String> BRANCH_ID = new ThreadLocal<>();
	private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();

	private TenantContext() {}

	public static void set(UUID orgId, String branchId, UUID userId) {
		ORG_ID.set(orgId);
		BRANCH_ID.set(branchId);
		USER_ID.set(userId);
	}

	public static UUID getOrgId() { return ORG_ID.get(); }
	public static String getBranchId() { return BRANCH_ID.get(); }
	public static UUID getUserId() { return USER_ID.get(); }

	public static void clear() {
		ORG_ID.remove();
		BRANCH_ID.remove();
		USER_ID.remove();
	}
}
