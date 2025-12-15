package org.nextme.promotion_service.monitoring.service;

import java.util.List;

import org.nextme.promotion_service.monitoring.analyzer.EnhancedAIAnalyzer;
import org.nextme.promotion_service.monitoring.collector.MetricsCollector;
import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.nextme.promotion_service.monitoring.detector.AnomalyDetector;
import org.nextme.promotion_service.monitoring.event.MonitoringEventPublisher;
import org.nextme.promotion_service.monitoring.event.MonitoringNotificationEvent;
import org.nextme.promotion_service.monitoring.history.MetricsHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitoring.enabled", havingValue = "true")
public class MonitoringService {

	private final MetricsCollector metricsCollector;
	private final EnhancedAIAnalyzer enhancedAIAnalyzer;
	private final MonitoringEventPublisher eventPublisher;
	private final MetricsHistoryService metricsHistoryService;
	private final AnomalyDetector anomalyDetector;

	@Value("${monitoring.notification.slack-user-ids}")
	private List<String> slackUserIds;

	@Value("${monitoring.anomaly-detection.enabled:true}")
	private boolean anomalyDetectionEnabled;

	/**
	 * 1분마다 메트릭 수집 및 이상 감지
	 * - 메트릭은 항상 Redis에 저장 (이력 관리용)
	 * - 임계치 초과 시에만 AI 분석 + Slack 알림
	 */
	@Scheduled(fixedRate = 60000)  // 1분마다 실행
	public void collectAndCheckAnomaly() {
		log.info("Starting metrics collection and anomaly detection...");

		try {
			// 1) 메트릭 수집
			SystemMetrics metrics = metricsCollector.collect();

			// 2) 메트릭 히스토리 저장 (항상 저장)
			metricsHistoryService.saveMetrics(metrics);
			log.info("Metrics saved to history");

			// 3) 이상 감지 체크
			if (anomalyDetectionEnabled && anomalyDetector.isAnomalyDetected(metrics)) {
				log.warn("Anomaly detected! Triggering AI analysis and notification...");

				// 이상 감지 시에만 AI 분석 + Slack 전송
				analyzeAndNotify(metrics);
			} else {
				log.info("No anomaly detected. Metrics within normal range.");
			}

		} catch (Exception e) {
			log.error("Failed to collect metrics or detect anomaly", e);
		}
	}

	/**
	 * 일일 보고서 생성 (스케줄러에서 호출)
	 * anomaly-detection과 별개로 정해진 시간에 보고서 전송
	 */
	@Async
	public void generateAndSendReport() {
		log.info("Starting scheduled daily report generation...");

		try {
			// 메트릭 수집
			SystemMetrics metrics = metricsCollector.collect();

			// 메트릭 히스토리 저장
			metricsHistoryService.saveMetrics(metrics);
			log.info("Metrics saved to history");

			// AI 분석 + Slack 전송
			analyzeAndNotify(metrics);

		} catch (Exception e) {
			log.error("Failed to generate or send monitoring report", e);
			throw new RuntimeException("Monitoring report generation failed", e);
		}
	}

	/**
	 * AI 분석 및 Slack 알림 전송 (공통 로직)
	 */
	private void analyzeAndNotify(SystemMetrics metrics) {
		try {
			// AI 분석 + 단일 해결방법 제시
			org.nextme.promotion_service.monitoring.analyzer.AnalysisResult result =
				enhancedAIAnalyzer.analyzeWithHistory(metrics);

			// 간단한 Slack 메시지 생성
			String message = buildSimpleMessage(result, metrics);

			// 버튼 포함 Slack 전송 (actionType을 actionValue로 전달)
			MonitoringNotificationEvent event = new MonitoringNotificationEvent(
				slackUserIds,
				message,
				"monitoring_action",
				result.getActionType()
			);
			eventPublisher.publishNotification(event);

			log.info("Alert notification sent successfully to {} users with action: {}",
				slackUserIds.size(), result.getActionType());

		} catch (Exception e) {
			log.error("Failed to analyze and notify", e);
		}
	}

	/**
	 * 간단한 Slack 메시지 생성 (버튼은 별도로 추가됨)
	 */
	private String buildSimpleMessage(
		org.nextme.promotion_service.monitoring.analyzer.AnalysisResult result,
		SystemMetrics metrics) {

		return String.format("""
				🚨 *시스템 이상 감지*

				📊 *현재 상태*
				CPU: %.2f%%
				메모리: %.2f%%
				HTTP 응답시간: %.2fms
				DB 커넥션: %d/%d

				*상황 분석*
				%s

				*해결 방안*
				%s

				*해결 근거*
				%s
				""",
			metrics.getCpuUsage(),
			metrics.getMemoryUsagePercent(),
			metrics.getHttpRequestMeanTime(),
			metrics.getDbConnectionActive(),
			metrics.getDbConnectionMax(),
			result.getAnalysis(),
			result.getRecommendation(),
			result.getReason()
		);
	}

	/**
	 * 테스트용 알림 발송 (Gemini API 없이 Kafka + Slack 연동 테스트)
	 */
	public void publishTestNotification(MonitoringNotificationEvent event) {
		log.info("Publishing test notification to Kafka...");
		try {
			eventPublisher.publishNotification(event);
			log.info("Test notification published successfully");
		} catch (Exception e) {
			log.error("Failed to publish test notification", e);
			throw new RuntimeException("Test notification failed", e);
		}
	}
}
