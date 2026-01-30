package com.cognitivequantum.billing.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Audit log helpers; use Correlation ID to trace credit deduction back to ML Training Job.
 */
public final class AuditLogUtil {

	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	public static final String MDC_CORRELATION_ID = "correlationId";

	private AuditLogUtil() {}

	/** Set or generate correlation ID for the current request; call at filter/controller. */
	public static String getOrCreateCorrelationId(String headerValue) {
		String id = headerValue != null && !headerValue.isBlank() ? headerValue.trim() : UUID.randomUUID().toString();
		MDC.put(MDC_CORRELATION_ID, id);
		return id;
	}

	/** Get current correlation ID from MDC (for logging). */
	public static String getCorrelationId() {
		return MDC.get(MDC_CORRELATION_ID);
	}

	/** Clear MDC (e.g. in filter after request). */
	public static void clearCorrelationId() {
		MDC.remove(MDC_CORRELATION_ID);
	}
}
