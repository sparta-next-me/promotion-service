package org.nextme.promotion_service.monitoring.service;

import java.util.List;

import org.nextme.promotion_service.monitoring.analyzer.AIAnalyzer;
import org.nextme.promotion_service.monitoring.client.NotificationClient;
import org.nextme.promotion_service.monitoring.client.dto.SlackUserMessageRequest;
import org.nextme.promotion_service.monitoring.collector.MetricsCollector;
import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.nextme.promotion_service.monitoring.report.ReportGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitoring.enabled", havingValue = "true")
public class MonitoringService {

	private final MetricsCollector metricsCollector;
	private final AIAnalyzer aiAnalyzer;
	private final ReportGenerator reportGenerator;
	private final NotificationClient notificationClient;

	@Value("${monitoring.notification.slack-user-ids}")
	private List<String> slackUserIds;

	// 모니터링 보고서를 생성하고 Slack으로 전송합니다.
	@Async
	public void generateAndSendReport() {
		log.info("Starting monitoring report generation...");

		try {
			// 메트릭 수집
			SystemMetrics metrics = metricsCollector.collect();

			// AI 분석
			String analysis = aiAnalyzer.analyze(metrics);

			// 보고서 생성
			String report = reportGenerator.generate(metrics, analysis);

			// Slack 전송
			SlackUserMessageRequest request = new SlackUserMessageRequest(slackUserIds, report);
			notificationClient.sendToUsers(request);

			log.info("Monitoring report sent successfully to {} users", slackUserIds.size());

		} catch (Exception e) {
			log.error("Failed to generate or send monitoring report", e);
			throw new RuntimeException("Monitoring report generation failed", e);
		}
	}
}
