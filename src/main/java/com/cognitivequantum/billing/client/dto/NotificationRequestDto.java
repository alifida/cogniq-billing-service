package com.cognitivequantum.billing.client.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request body for notification service POST /internal/send.
 */
public record NotificationRequestDto(
	UUID orgId,
	UUID recipientId,
	String recipientEmail,
	String templateId,
	List<String> channels,
	Map<String, Object> params
) {}
