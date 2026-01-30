package com.cognitivequantum.billing.entity;

/**
 * Types of billable usage for limits and aggregation.
 */
public enum UsageType {
	COMPUTE_HOURS,
	TEAM_SEATS,
	DATASET_COUNT,
	TRAINING_JOBS,
	API_CALLS
}
