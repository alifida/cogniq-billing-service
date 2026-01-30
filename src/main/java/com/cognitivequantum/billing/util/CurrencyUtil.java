package com.cognitivequantum.billing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Currency converters and formatting for billing.
 */
public final class CurrencyUtil {

	private CurrencyUtil() {}

	/** Convert cents to decimal amount (e.g. 49900 -> 499.00). */
	public static BigDecimal centsToAmount(long cents) {
		return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}

	/** Convert decimal amount to cents (e.g. 499.00 -> 49900). */
	public static long amountToCents(BigDecimal amount) {
		return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
	}

	/** Format amount for display (e.g. "$499.00"). */
	public static String formatUsd(BigDecimal amount) {
		if (amount == null) return "$0.00";
		return String.format("$%.2f", amount.setScale(2, RoundingMode.HALF_UP));
	}

	/** Format cents for display (e.g. "$499.00"). */
	public static String formatUsdCents(long cents) {
		return formatUsd(centsToAmount(cents));
	}
}
