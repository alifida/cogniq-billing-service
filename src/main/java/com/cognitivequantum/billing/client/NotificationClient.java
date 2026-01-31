package com.cognitivequantum.billing.client;

import com.cognitivequantum.billing.client.dto.NotificationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client to Notification service internal /send. Not exposed via Gateway.
 */
@FeignClient(name = "cogniq-notification-service", path = "/internal", fallback = NotificationClientFallback.class)
public interface NotificationClient {

	@PostMapping("/send")
	void send(@RequestBody NotificationRequestDto request);
}
