package org.nextme.promotion_service.monitoring.history;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsHistoryService {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String METRICS_KEY_PREFIX = "metrics:history:";
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
	private static final int RETENTION_HOURS = 24;

	/**
	 * 메트릭을 Redis에 저장
	 * Key: metrics:history:2025-12-15-14-30
	 * TTL: 24시간
	 */
	public void saveMetrics(SystemMetrics metrics) {
		try {
			String timestamp = LocalDateTime.now().format(FORMATTER);
			String key = METRICS_KEY_PREFIX + timestamp;
			String value = objectMapper.writeValueAsString(metrics);

			redisTemplate.opsForValue().set(key, value, RETENTION_HOURS, TimeUnit.HOURS);
			log.debug("Metrics saved: {}", key);
		} catch (JsonProcessingException e) {
			log.error("Failed to save metrics", e);
		}
	}

	/**
	 * 지난 N시간의 메트릭 조회
	 */
	public List<SystemMetrics> getRecentMetrics(int hours) {
		List<SystemMetrics> metrics = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();

		for (int i = 0; i < hours * 60; i += 1) { // 1분 단위
			LocalDateTime timestamp = now.minusMinutes(i);
			String key = METRICS_KEY_PREFIX + timestamp.format(FORMATTER);
			String value = redisTemplate.opsForValue().get(key);

			if (value != null) {
				try {
					SystemMetrics metric = objectMapper.readValue(value, SystemMetrics.class);
					metrics.add(metric);
				} catch (JsonProcessingException e) {
					log.warn("Failed to parse metrics: {}", key);
				}
			}
		}

		return metrics;
	}

	/**
	 * 메트릭 통계 계산
	 */
	public MetricsStatistics calculateStatistics(List<SystemMetrics> metrics) {
		if (metrics.isEmpty()) {
			return MetricsStatistics.empty();
		}

		double avgCpu = metrics.stream()
			.mapToDouble(SystemMetrics::getCpuUsage)
			.average()
			.orElse(0.0);

		double maxCpu = metrics.stream()
			.mapToDouble(SystemMetrics::getCpuUsage)
			.max()
			.orElse(0.0);

		double minCpu = metrics.stream()
			.mapToDouble(SystemMetrics::getCpuUsage)
			.min()
			.orElse(0.0);

		double avgMemory = metrics.stream()
			.mapToDouble(SystemMetrics::getMemoryUsagePercent)
			.average()
			.orElse(0.0);

		double maxMemory = metrics.stream()
			.mapToDouble(SystemMetrics::getMemoryUsagePercent)
			.max()
			.orElse(0.0);

		double avgResponseTime = metrics.stream()
			.mapToDouble(SystemMetrics::getHttpRequestMeanTime)
			.average()
			.orElse(0.0);

		double maxResponseTime = metrics.stream()
			.mapToDouble(SystemMetrics::getHttpRequestMaxTime)
			.max()
			.orElse(0.0);

		return MetricsStatistics.builder()
			.avgCpu(avgCpu)
			.maxCpu(maxCpu)
			.minCpu(minCpu)
			.avgMemory(avgMemory)
			.maxMemory(maxMemory)
			.avgResponseTime(avgResponseTime)
			.maxResponseTime(maxResponseTime)
			.dataPoints(metrics.size())
			.build();
	}
}
