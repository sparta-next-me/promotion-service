package org.nextme.promotion_service.monitoring.collector;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.TimeUnit;

import org.nextme.infrastructure.exception.ApplicationException;
import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.nextme.promotion_service.monitoring.exception.MonitoringErrorCode;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsCollector {

	private final MeterRegistry meterRegistry;

	public SystemMetrics collect() {
		try {
			log.info("Collecting system metrics...");

			// CPU 사용률
			double cpuUsage = getCpuUsage();

			// 메모리 사용량
			MemoryUsage heapMemory = getHeapMemoryUsage();
			long memoryUsed = heapMemory.getUsed() / (1024 * 1024);
			long memoryMax = heapMemory.getMax() / (1024 * 1024);
			double memoryUsagePercent = (double)heapMemory.getUsed() / heapMemory.getMax() * 100;

			// HTTP 요청 통계
			Timer httpTimer = meterRegistry.find("http.server.requests").timer();
			long httpRequestCount = httpTimer != null ? httpTimer.count() : 0;
			double httpRequestMeanTime = httpTimer != null ? httpTimer.mean(TimeUnit.MILLISECONDS) : 0.0;
			double httpRequestMaxTime = httpTimer != null ? httpTimer.max(TimeUnit.MILLISECONDS) : 0.0;

			// DB 커넥션 풀
			int dbConnectionActive = getGaugeValue("hikaricp.connections.active");
			int dbConnectionIdle = getGaugeValue("hikaricp.connections.idle");
			int dbConnectionMax = getGaugeValue("hikaricp.connections.max");

			// 시스템 가동 시간
			double uptimeSeconds = getGaugeValue("process.uptime");

			SystemMetrics metrics = SystemMetrics.builder()
				.cpuUsage(cpuUsage)
				.memoryUsed(memoryUsed)
				.memoryMax(memoryMax)
				.memoryUsagePercent(memoryUsagePercent)
				.httpRequestCount(httpRequestCount)
				.httpRequestMeanTime(httpRequestMeanTime)
				.httpRequestMaxTime(httpRequestMaxTime)
				.dbConnectionActive(dbConnectionActive)
				.dbConnectionIdle(dbConnectionIdle)
				.dbConnectionMax(dbConnectionMax)
				.uptimeSeconds(uptimeSeconds)
				.build();

			log.info("Metrics collected successfully:\n{}", metrics);
			return metrics;

		} catch (Exception e) {
			log.error("Failed to collect metrics", e);
			throw new ApplicationException(
				MonitoringErrorCode.METRICS_COLLECTION_FAILED.getHttpStatus(),
				MonitoringErrorCode.METRICS_COLLECTION_FAILED.getCode(),
				"메트릭 수집 중 오류가 발생했습니다: " + e.getMessage()
			);
		}
	}

	private double getCpuUsage() {
		try {
			return meterRegistry.find("system.cpu.usage")
				.gauge()
				.value() * 100; // 백분율로 변환
		} catch (Exception e) {
			log.warn("Failed to get CPU usage, returning 0", e);
			return 0.0;
		}
	}

	private MemoryUsage getHeapMemoryUsage() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		return memoryMXBean.getHeapMemoryUsage();
	}

	private int getGaugeValue(String metricName) {
		try {
			return (int)meterRegistry.find(metricName)
				.gauge()
				.value();
		} catch (Exception e) {
			log.warn("Failed to get gauge value for {}, returning 0", metricName, e);
			return 0;
		}
	}
}
