package org.nextme.promotion_service.monitoring.detector;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nextme.promotion_service.monitoring.client.NotificationClient;
import org.nextme.promotion_service.monitoring.client.dto.SlackUserMessageRequest;
import org.nextme.promotion_service.monitoring.collector.MetricsCollector;
import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitoring.anomaly-detection.enabled", havingValue = "true")
public class AnomalyDetector {

	private final MetricsCollector metricsCollector;
	private final NotificationClient notificationClient;

	@Value("${monitoring.notification.slack-user-ids}")
	private List<String> slackUserIds;

	@Value("${monitoring.anomaly-detection.thresholds.cpu-usage:80.0}")
	private double cpuThreshold;

	@Value("${monitoring.anomaly-detection.thresholds.memory-usage:85.0}")
	private double memoryThreshold;

	@Value("${monitoring.anomaly-detection.thresholds.response-time:1000.0}")
	private double responseTimeThreshold;

	@Value("${monitoring.anomaly-detection.thresholds.db-connection:90.0}")
	private double dbConnectionThreshold;

	@Value("${monitoring.anomaly-detection.cooldown-minutes:15}")
	private int cooldownMinutes;

	// 알림 쿨다운 관리
	private final Map<String, LocalDateTime> alertCooldowns = new ConcurrentHashMap<>();

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	// 1분마다 메트릭 확인 및 이상 탐지 (수동 호출도 가능)
	@Scheduled(fixedRateString = "${monitoring.anomaly-detection.interval:60000}")
	public void detectAnomalies() {
		try {
			log.debug("Starting anomaly detection...");

			SystemMetrics metrics = metricsCollector.collect();
			List<String> alerts = new ArrayList<>();

			// CPU 이상 감지
			if (metrics.getCpuUsage() > cpuThreshold) {
				String alert = String.format("CPU 사용률 위험 : %.2f%% (임계값 : %.2f%%)",
					metrics.getCpuUsage(), cpuThreshold);
				alerts.add(alert);
				log.warn(alert);
			}

			// 메모리 이상 감지
			if (metrics.getMemoryUsagePercent() > memoryThreshold) {
				String alert = String.format("메모리 부족 경고 : %.2f%% (임계값 : %.2f%%)",
					metrics.getMemoryUsagePercent(), memoryThreshold);
				alerts.add(alert);
				log.warn(alert);
			}

			// HTTP 응답 시간 이상 감지
			if (metrics.getHttpRequestMaxTime() > responseTimeThreshold) {
				String alert = String.format("응답 시간 지연 : %.2fms (임계값 : %.2fms)",
					metrics.getHttpRequestMaxTime(), responseTimeThreshold);
				alerts.add(alert);
				log.warn(alert);
			}

			// DB 커넥션 고갈 위험
			if (metrics.getDbConnectionMax() > 0) {
				double connectionUsage = (double)metrics.getDbConnectionActive() / metrics.getDbConnectionMax() * 100;
				if (connectionUsage > dbConnectionThreshold) {
					String alert = String.format("DB 커넥션 부족 : %d / %d (%.2f%%, 임계값 : %.2f%%)",
						metrics.getDbConnectionActive(),
						metrics.getDbConnectionMax(),
						connectionUsage,
						dbConnectionThreshold);
					alerts.add(alert);
					log.warn(alert);
				}
			}

			// 이상이 감지되면 즉시 알림
			if (!alerts.isEmpty() && canSendAlert("general")) {
				sendUrgentAlert(metrics, alerts);
			}
		} catch (Exception e) {
			log.error("Anomaly detection failed", e);
		}
	}

	// 알림 쿨다운 체크 (중복 알림 방지)
	private boolean canSendAlert(String alertType) {
		LocalDateTime lastSent = alertCooldowns.get(alertType);
		if (lastSent != null) {
			long minutesSinceLastAlert = Duration.between(lastSent, LocalDateTime.now()).toMinutes();
			if (minutesSinceLastAlert < cooldownMinutes) {
				log.debug("Alert cooldown active. Last sent {} minutes ago", minutesSinceLastAlert);
				return false;
			}
		}
		alertCooldowns.put(alertType, LocalDateTime.now());
		return true;
	}

	// 긴급 알림 전송
	private void sendUrgentAlert(SystemMetrics metrics, List<String> alerts) {
		String timestamp = LocalDateTime.now().format(FORMATTER);

		String message = String.format("""
				🚨 *서버 이상 감지*
				
				*탐지 시간*: %s
				
				*감지된 문제*
				%s
				
				*현재 메트릭*
				• CPU 사용률: %.2f%%
				• 메모리 사용률: %.2f%%
				• HTTP 평균 응답: %.2fms
				• HTTP 최대 응답: %.2fms
				• DB 커넥션: %d/%d
				
				⚡ 즉시 확인이 필요합니다!
				""",
			timestamp,
			String.join("\n", alerts),
			metrics.getCpuUsage(),
			metrics.getMemoryUsagePercent(),
			metrics.getHttpRequestMeanTime(),
			metrics.getHttpRequestMaxTime(),
			metrics.getDbConnectionActive(),
			metrics.getDbConnectionMax()
		);

		try {
			SlackUserMessageRequest request = new SlackUserMessageRequest(slackUserIds, message);
			notificationClient.sendToUsers(request);
			log.info("Urgent alert sent successfully : {} issues detected", alerts.size());
		} catch (Exception e) {
			log.error("Failed to send urgent alert", e);
		}
	}
}
