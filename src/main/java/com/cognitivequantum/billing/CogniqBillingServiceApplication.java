package com.cognitivequantum.billing;

import com.cognitivequantum.billing.entity.Plan;
import com.cognitivequantum.billing.repository.PlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@SpringBootApplication
public class CogniqBillingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CogniqBillingServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner seedPlans(PlanRepository planRepository) {
		return args -> {
			if (planRepository.existsBySlug("FREE")) return;
			planRepository.save(Plan.builder()
				.name("Free")
				.slug("FREE")
				.priceAmountCents(0L)
				.currency("USD")
				.interval(Plan.BillingInterval.MONTHLY)
				.limits(Map.of("compute_hours", 10, "team_seats", 1, "dataset_count", 5, "training_jobs", 10))
				.features(Map.of("description", "Starter tier for experimentation"))
				.active(true)
				.sortOrder(0)
				.build());
			planRepository.save(Plan.builder()
				.name("Pro")
				.slug("PRO")
				.priceAmountCents(9900L)
				.currency("USD")
				.interval(Plan.BillingInterval.MONTHLY)
				.limits(Map.of("compute_hours", 100, "team_seats", 5, "dataset_count", 50, "training_jobs", 200))
				.features(Map.of("description", "For small teams and production workloads"))
				.active(true)
				.sortOrder(1)
				.build());
			planRepository.save(Plan.builder()
				.name("Enterprise Quantum")
				.slug("ENTERPRISE")
				.priceAmountCents(49900L)
				.currency("USD")
				.interval(Plan.BillingInterval.MONTHLY)
				.limits(Map.of("compute_hours", 500, "team_seats", 20, "dataset_count", 500, "training_jobs", 2000))
				.features(Map.of(
					"description", "Dedicated GPU Node, 24/7 Priority Support, Custom API Limits",
					"perks", java.util.List.of("Dedicated GPU Node", "24/7 Priority Support", "Custom API Limits")))
				.active(true)
				.sortOrder(2)
				.build());
		};
	}
}
