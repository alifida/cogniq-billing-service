package com.cognitivequantum.billing.config;

import java.util.UUID;

/**
 * Principal holding the current user id for ownership checks.
 */
public record UserIdPrincipal(UUID userId) {
}
