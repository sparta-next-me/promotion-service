package org.nextme.promotion_service.monitoring.history;

import lombok.Builder;
import lombok.Getter;

/**
 * 메트릭 통계 DTO
 */
@Getter
@Builder
public class MetricsStatistics {

	private final double avgCpu;
	private final double maxCpu;
	private final double minCpu;
	private final double avgMemory;
	private final double maxMemory;
	private final double avgResponseTime;
	private final double maxResponseTime;
	private final int dataPoints;

	public static MetricsStatistics empty() {
		return MetricsStatistics.builder()
			.avgCpu(0.0)
			.maxCpu(0.0)
			.minCpu(0.0)
			.avgMemory(0.0)
			.maxMemory(0.0)
			.avgResponseTime(0.0)
			.maxResponseTime(0.0)
			.dataPoints(0)
			.build();
	}
}
