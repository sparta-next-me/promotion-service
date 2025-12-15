package org.nextme.promotion_service.monitoring.test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 테스트용 과거 메트릭 데이터 주입기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataInjector {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String METRICS_KEY_PREFIX = "metrics:history:";
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
	private static final int RETENTION_HOURS = 24;

	/**
	 * 시나리오: 점진적 메모리 누수
	 * - 6시간 전: 메모리 50% (정상)
	 * - 3시간 전: 메모리 65% (증가 중)
	 * - 1시간 전: 메모리 78% (위험)
	 * - 현재: 메모리 90% (임계치 초과!) 🚨
	 */
	public void injectMemoryLeakScenario() {
		log.info("=== Injecting Memory Leak Scenario ===");

		LocalDateTime now = LocalDateTime.now();

		// 6시간 전부터 현재까지 1분 단위로 데이터 생성 (361개)
		for (int minutesAgo = 360; minutesAgo >= 0; minutesAgo--) {
			LocalDateTime timestamp = now.minusMinutes(minutesAgo);

			// 시간에 따라 점진적으로 메모리 증가
			double memoryUsagePercent = calculateMemoryUsage(minutesAgo);
			double cpuUsage = calculateCpuUsage(minutesAgo);

			SystemMetrics metrics = SystemMetrics.builder()
				.cpuUsage(cpuUsage)
				.memoryUsed((long) (8192 * memoryUsagePercent / 100))  // 8GB 중 사용량
				.memoryMax(8192L)
				.memoryUsagePercent(memoryUsagePercent)
				.httpRequestCount(100L + (minutesAgo % 50))
				.httpRequestMeanTime(80.0 + (minutesAgo % 30))
				.httpRequestMaxTime(200.0 + (minutesAgo % 100))
				.dbConnectionActive(10 + (minutesAgo % 5))
				.dbConnectionIdle(30)
				.dbConnectionMax(50)
				.uptimeSeconds((360 - minutesAgo) * 60.0)
				.build();

			saveMetrics(timestamp, metrics);

			// 로그는 30분마다만 출력 (너무 많아서)
			if (minutesAgo % 30 == 0 || minutesAgo == 0) {
				log.info("Injected: {} - Memory: {:.2f}%, CPU: {:.2f}%",
					timestamp.format(FORMATTER), memoryUsagePercent, cpuUsage);
			}
		}

		// ✅ 중요: 현재 시점 (now)에도 임계치 초과 데이터 저장
		double currentMemory = 90.0;  // 임계치 85% 초과!
		double currentCpu = 55.0;
		SystemMetrics currentMetrics = SystemMetrics.builder()
			.cpuUsage(currentCpu)
			.memoryUsed((long) (8192 * currentMemory / 100))
			.memoryMax(8192L)
			.memoryUsagePercent(currentMemory)
			.httpRequestCount(150L)
			.httpRequestMeanTime(95.0)
			.httpRequestMaxTime(250.0)
			.dbConnectionActive(12)
			.dbConnectionIdle(28)
			.dbConnectionMax(50)
			.uptimeSeconds(21600.0)
			.build();

		saveMetrics(now, currentMetrics);
		log.info("✅ Current timestamp injected: {} - Memory: {:.2f}%, CPU: {:.2f}%",
			now.format(FORMATTER), currentMemory, currentCpu);

		log.info("=== Injection Complete: 361 data points (including current) ===");
	}

	/**
	 * 시나리오: CPU 급증 (캐시 미스)
	 * - 2시간 전: CPU 30% (정상)
	 * - 1시간 전: CPU 50% (증가)
	 * - 30분 전: CPU 70% (급증)
	 * - 현재: CPU 95% (임계치 초과!) 🚨
	 */
	public void injectCpuSpikeScenario() {
		log.info("=== Injecting CPU Spike Scenario ===");

		LocalDateTime now = LocalDateTime.now();

		// 2시간 전부터 1분 단위로 데이터 생성 (120개)
		for (int minutesAgo = 120; minutesAgo >= 0; minutesAgo--) {
			LocalDateTime timestamp = now.minusMinutes(minutesAgo);

			// 급격한 CPU 증가 패턴
			double cpuUsage = calculateCpuSpike(minutesAgo);
			double memoryUsagePercent = 60.0 + (Math.random() * 5);  // 메모리는 정상

			SystemMetrics metrics = SystemMetrics.builder()
				.cpuUsage(cpuUsage)
				.memoryUsed(4915L)
				.memoryMax(8192L)
				.memoryUsagePercent(memoryUsagePercent)
				.httpRequestCount(1000L + (minutesAgo * 10))  // 트래픽 증가
				.httpRequestMeanTime(500.0 + (120 - minutesAgo) * 5)  // 응답시간 증가
				.httpRequestMaxTime(1200.0 + (120 - minutesAgo) * 8)
				.dbConnectionActive(25 + (minutesAgo % 10))
				.dbConnectionIdle(15)
				.dbConnectionMax(50)
				.uptimeSeconds((120 - minutesAgo) * 60.0)
				.build();

			saveMetrics(timestamp, metrics);

			if (minutesAgo % 15 == 0) {
				log.info("Injected: {} - CPU: {:.2f}%, Memory: {:.2f}%",
					timestamp.format(FORMATTER), cpuUsage, memoryUsagePercent);
			}
		}

		log.info("=== Injection Complete: 120 data points ===");
	}

	/**
	 * 메모리 누수 패턴 계산
	 * 6시간(360분) 동안 50% → 90%로 점진적 증가
	 */
	private double calculateMemoryUsage(int minutesAgo) {
		// 6시간 전 = 50%, 현재 = 90%
		// 선형 증가: y = 50 + (40 / 360) * (360 - minutesAgo)
		double baseMemory = 50.0;
		double increment = 40.0;
		double progress = (360.0 - minutesAgo) / 360.0;

		// 약간의 노이즈 추가 (현실감)
		double noise = (Math.random() - 0.5) * 3;

		return baseMemory + (increment * progress) + noise;
	}

	/**
	 * CPU 변동 패턴 (메모리와 함께 증가)
	 */
	private double calculateCpuUsage(int minutesAgo) {
		// 메모리 증가에 따라 CPU도 약간 증가
		double baseCpu = 40.0;
		double memoryBasedIncrease = (360.0 - minutesAgo) / 360.0 * 15.0;
		double noise = (Math.random() - 0.5) * 10;

		return baseCpu + memoryBasedIncrease + noise;
	}

	/**
	 * CPU 급증 패턴 계산
	 * 2시간(120분) 동안 30% → 95%로 급증
	 */
	private double calculateCpuSpike(int minutesAgo) {
		if (minutesAgo > 60) {
			// 1시간 전까지는 정상 (30-40%)
			return 30.0 + (Math.random() * 10);
		} else if (minutesAgo > 30) {
			// 30-60분 전: 급증 시작 (40-70%)
			double progress = (60.0 - minutesAgo) / 30.0;
			return 40.0 + (progress * 30.0) + (Math.random() * 5);
		} else {
			// 최근 30분: 급등 (70-95%)
			double progress = (30.0 - minutesAgo) / 30.0;
			return 70.0 + (progress * 25.0) + (Math.random() * 3);
		}
	}

	/**
	 * Redis에 메트릭 저장
	 */
	private void saveMetrics(LocalDateTime timestamp, SystemMetrics metrics) {
		try {
			String key = METRICS_KEY_PREFIX + timestamp.format(FORMATTER);
			String value = objectMapper.writeValueAsString(metrics);
			redisTemplate.opsForValue().set(key, value, RETENTION_HOURS, TimeUnit.HOURS);
		} catch (Exception e) {
			log.error("Failed to inject test data at {}", timestamp, e);
		}
	}

	/**
	 * 테스트 데이터 삭제
	 */
	public void clearTestData() {
		log.info("Clearing all test data from Redis...");
		redisTemplate.keys(METRICS_KEY_PREFIX + "*").forEach(redisTemplate::delete);
		log.info("Test data cleared");
	}
}
