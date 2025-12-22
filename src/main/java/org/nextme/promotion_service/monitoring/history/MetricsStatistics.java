package org.nextme.promotion_service.monitoring.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메트릭 통계 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsStatistics {

	private double avgCpu;
	private double maxCpu;
	private double minCpu;
	private double avgMemory;
	private double maxMemory;
	private double avgResponseTime;
	private double maxResponseTime;
	private int dataPoints;

	/**
	 * 빈 통계 객체 생성
	 */
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
