package com.cognitivequantum.billing.config;

import com.cognitivequantum.billing.repository.SubscriptionRepository;
import com.cognitivequantum.billing.entity.SubscriptionStatus;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PrometheusMetricsConfig {

	private final SubscriptionRepository subscriptionRepository;

	@Bean
	public MeterBinder subscriptionsGauge(MeterRegistry registry) {
		return registry1 -> Gauge.builder("active_subscriptions_gauge", subscriptionRepository,
				repo -> repo.findAll().stream().filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE).count())
			.description("Number of active subscriptions")
			.register(registry1);
	}
}
