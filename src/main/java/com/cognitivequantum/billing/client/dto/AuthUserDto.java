package com.cognitivequantum.billing.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDto {

	private UUID id;
	private String email;
	private String fullName;
	/** Organization ID (billing entity); from Auth service internal user endpoint */
	private UUID orgId;
}
