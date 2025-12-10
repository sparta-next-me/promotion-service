package org.nextme.promotion_service.monitoring.collector.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SystemMetrics {

	// CPU 사용률
	private final double cpuUsage;

	// 메모리 (MB 단위)
	private final long memoryUsed;
	private final long memoryMax;
	private final double memoryUsagePercent;

	// HTTP 요청 통계
	private final long httpRequestCount;
	private final double httpRequestMeanTime;
	private final double httpRequestMaxTime;

	// 데이터베이스 커넥션 풀
	private final int dbConnectionActive;
	private final int dbConnectionIdle;
	private final int dbConnectionMax;

	// 시스템 가동 시간 (초)
	private final double uptimeSeconds;

	@Override
	public String toString() {
		return String.format(
			"=== System Metrics ===\n" +
			"CPU Usage: %.2f%%\n" +
			"Memory: %dMB / %dMB (%.2f%%)\n" +
			"HTTP Requests: %d (평균: %.2fms, 최대: %.2fms)\n" +
			"DB Connections: %d active / %d idle / %d max\n" +
			"Uptime: %.2f seconds",
			cpuUsage,
			memoryUsed, memoryMax, memoryUsagePercent,
			httpRequestCount, httpRequestMeanTime, httpRequestMaxTime,
			dbConnectionActive, dbConnectionIdle, dbConnectionMax,
			uptimeSeconds
		);
	}
}
