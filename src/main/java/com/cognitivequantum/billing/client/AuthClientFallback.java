package com.cognitivequantum.billing.client;

import com.cognitivequantum.billing.client.dto.AuthUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class AuthClientFallback implements AuthClient {

	@Override
	public AuthUserDto getUserById(UUID userId) {
		log.warn("Auth client fallback: getUserById({}) - Auth service unavailable", userId);
		return null;
	}
}
