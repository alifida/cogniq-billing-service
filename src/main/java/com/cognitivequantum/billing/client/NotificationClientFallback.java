package com.cognitivequantum.billing.client;

import com.cognitivequantum.billing.client.dto.NotificationRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

	@Override
	public void send(NotificationRequestDto request) {
		log.warn("Notification service unavailable, skipping send for template {}", request != null ? request.templateId() : "?");
	}
}
