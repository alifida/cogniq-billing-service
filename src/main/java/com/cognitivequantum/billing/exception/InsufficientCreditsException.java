package com.cognitivequantum.billing.exception;

/**
 * Thrown when user has 0 available credits (402 Payment Required).
 */
public class InsufficientCreditsException extends RuntimeException {

	public InsufficientCreditsException(String message) {
		super(message);
	}
}
