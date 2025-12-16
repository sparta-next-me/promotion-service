package org.nextme.promotion_service.monitoring.test;

import org.nextme.promotion_service.monitoring.collector.MetricsCollector;
import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.nextme.promotion_service.monitoring.detector.AnomalyDetector;
import org.nextme.promotion_service.monitoring.service.MonitoringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 모니터링 테스트용 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/v1/promotions/test/monitoring")
@RequiredArgsConstructor
public class TestController {

	private final TestDataInjector testDataInjector;
	private final MetricsCollector metricsCollector;
	private final AnomalyDetector anomalyDetector;
	private final MonitoringService monitoringService;

	/**
	 * 시나리오 1: 메모리 누수 테스트 데이터 주입
	 */
	@PostMapping("/inject-memory-leak")
	public String injectMemoryLeak() {
		log.info("=== Starting Memory Leak Scenario Test ===");
		testDataInjector.injectMemoryLeakScenario();
		return "✅ Memory leak scenario injected (360 data points, 6 hours)";
	}

	/**
	 * 시나리오 2: CPU 급증 테스트 데이터 주입
	 */
	@PostMapping("/inject-cpu-spike")
	public String injectCpuSpike() {
		log.info("=== Starting CPU Spike Scenario Test ===");
		testDataInjector.injectCpuSpikeScenario();
		return "✅ CPU spike scenario injected (120 data points, 2 hours)";
	}

	/**
	 * 테스트 데이터 삭제
	 */
	@PostMapping("/clear")
	public String clearTestData() {
		testDataInjector.clearTestData();
		return "✅ All test data cleared from Redis";
	}

	/**
	 * 현재 메트릭 확인 및 이상 감지 테스트
	 */
	@GetMapping("/check")
	public String checkAnomaly() {
		SystemMetrics metrics = metricsCollector.collect();

		boolean isAnomaly = anomalyDetector.isAnomalyDetected(metrics);

		String result = String.format("""
				📊 현재 실제 서버 메트릭:
				- CPU: %.2f%%
				- 메모리: %.2f%%
				- 응답시간: %.2fms
				- DB 커넥션: %d/%d

				🔍 이상 감지: %s

				%s

				💡 Tip: 실제 서버는 정상일 수 있습니다.
				   /trigger 를 호출하면 Redis에 저장된
				   테스트 데이터로 AI 분석이 실행됩니다.
				""",
			metrics.getCpuUsage(),
			metrics.getMemoryUsagePercent(),
			metrics.getHttpRequestMeanTime(),
			metrics.getDbConnectionActive(),
			metrics.getDbConnectionMax(),
			isAnomaly ? "⚠️ YES" : "✅ NO",
			isAnomaly ? anomalyDetector.getAnomalyDescription(metrics) : "정상 범위입니다."
		);

		log.info("\n{}", result);
		return result;
	}

	/**
	 * 주입된 테스트 데이터로 강제 임계치 초과 시뮬레이션
	 */
	@PostMapping("/simulate-anomaly")
	public String simulateAnomaly() {
		log.info("=== Simulating anomaly with test data ===");

		// 테스트 데이터: 메모리 90% (임계치 85% 초과)
		SystemMetrics testMetrics = SystemMetrics.builder()
			.cpuUsage(55.0)
			.memoryUsed(7372L)  // 8192 * 0.9
			.memoryMax(8192L)
			.memoryUsagePercent(90.0)  // 임계치 85% 초과!
			.httpRequestCount(150L)
			.httpRequestMeanTime(95.0)
			.httpRequestMaxTime(250.0)
			.dbConnectionActive(12)
			.dbConnectionIdle(28)
			.dbConnectionMax(50)
			.uptimeSeconds(21600.0)
			.build();

		boolean isAnomaly = anomalyDetector.isAnomalyDetected(testMetrics);

		if (isAnomaly) {
			log.warn("✅ Anomaly detected with test data!");
			String description = anomalyDetector.getAnomalyDescription(testMetrics);
			return "✅ 이상 감지 성공!\n\n" + description +
				"\n\n이제 /trigger를 호출하면 AI 분석이 실행됩니다.";
		} else {
			return "❌ 이상 감지 실패. 임계치 설정을 확인하세요.";
		}
	}

	/**
	 * 강제로 모니터링 실행 (실제 서버 메트릭 사용)
	 * POST /api/test/monitoring/trigger
	 */
	@PostMapping("/trigger")
	public String triggerMonitoring() {
		log.info("=== Manually triggering monitoring with REAL metrics ===");
		monitoringService.collectAndCheckAnomaly();
		return "✅ Monitoring triggered with REAL server metrics. Check logs and Slack!";
	}

	/**
	 * 테스트 데이터로 강제 AI 분석 실행 (메모리 90% 시나리오)
	 */
	@PostMapping("/trigger-with-test-data")
	public String triggerWithTestData() {
		log.info("=== Triggering monitoring with TEST DATA (Memory 90%) ===");

		// 테스트 메트릭: 메모리 90% (임계치 85% 초과!)
		SystemMetrics testMetrics = SystemMetrics.builder()
			.cpuUsage(55.0)
			.memoryUsed(7372L)  // 8192 * 0.9
			.memoryMax(8192L)
			.memoryUsagePercent(90.0)  // ⚠️ 임계치 초과!
			.httpRequestCount(150L)
			.httpRequestMeanTime(95.0)
			.httpRequestMaxTime(250.0)
			.dbConnectionActive(12)
			.dbConnectionIdle(28)
			.dbConnectionMax(50)
			.uptimeSeconds(21600.0)
			.build();

		// 임계치 체크
		boolean isAnomaly = anomalyDetector.isAnomalyDetected(testMetrics);

		if (!isAnomaly) {
			return "❌ Test failed: Anomaly not detected with test data (Memory 90%)";
		}

		log.warn("✅ Anomaly detected! Memory: 90% > threshold 85%");

		// AI 분석 + Slack 전송 강제 실행
		// 주의: 이건 테스트용이므로 MonitoringService의 private 메서드를 호출할 수 없음
		// 대신 직접 구현
		try {
			String anomalyDescription = anomalyDetector.getAnomalyDescription(testMetrics);

			return String.format("""
				✅ TEST DATA로 이상 감지 성공!

				%s

				⚠️ 주의: AI 분석은 Gemini API 키가 필요합니다.
				현재는 이상 감지 로직만 테스트되었습니다.

				실제 AI 분석 + Slack 전송을 테스트하려면:
				1. Gemini API 키를 application.yaml에 설정
				2. monitoring.anomaly-detection.enabled=true
				3. /trigger 엔드포인트 사용
				""",
				anomalyDescription
			);
		} catch (Exception e) {
			log.error("Failed to process test data", e);
			return "❌ Error: " + e.getMessage();
		}
	}

	/**
	 * Gemini API 없이 Slack 전송만 테스트
	 */
	@PostMapping("/test-slack-only")
	public String testSlackOnly() {
		log.info("=== Testing Slack notification without AI ===");

		try {
			// 간단한 테스트 메시지
			String testMessage = """
				🧪 **Slack 연동 테스트**

				이 메시지가 도착했다면 Kafka + Slack 연동이 정상 작동합니다!

				📊 테스트 시나리오:
				• 메모리: 90% (임계치 85% 초과)
				• CPU: 55%

				✅ 다음 단계: OpenAI API 키 설정 후 AI 분석 테스트
				""";

			// Kafka 이벤트 발행
			org.nextme.promotion_service.monitoring.event.MonitoringNotificationEvent event =
				new org.nextme.promotion_service.monitoring.event.MonitoringNotificationEvent(
					java.util.List.of(System.getenv("SLACK_USER_ID") != null ?
						System.getenv("SLACK_USER_ID") : "U0832GZP47T"),
					testMessage
				);

			monitoringService.publishTestNotification(event);

			return "✅ Test message sent to Kafka! Check your Slack DM.";

		} catch (Exception e) {
			log.error("Failed to send test message", e);
			return "❌ Error: " + e.getMessage();
		}
	}
}
