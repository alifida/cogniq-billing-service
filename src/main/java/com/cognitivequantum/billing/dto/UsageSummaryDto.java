package com.cognitivequantum.billing.dto;

import com.cognitivequantum.billing.entity.UsageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Usage summary for current period (used / limit)")
public class UsageSummaryDto {

	@Schema(description = "Usage type to used quantity")
	private Map<UsageType, Long> used;

	@Schema(description = "Usage type to limit (from plan)")
	private Map<UsageType, Integer> limits;

	@Schema(description = "Usage type to percentage used (0-100)")
	private Map<UsageType, Integer> percentUsed;
}
