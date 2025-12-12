package org.nextme.promotion_service.monitoring.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.nextme.infrastructure.exception.ApplicationException;
import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.nextme.promotion_service.monitoring.exception.MonitoringErrorCode;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReportGenerator {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public String generate(SystemMetrics metrics, String aiAnalysis) {
		try {
			log.info("Generating monitoring report");

			String report = buildReport(metrics, aiAnalysis);
			log.info("Report generated successfully: {} characters", report.length());

			return report;
		} catch (Exception e) {
			log.error("Failed to generate report", e);
			throw new ApplicationException(
				MonitoringErrorCode.REPORT_GENERATION_FAILED.getHttpStatus(),
				MonitoringErrorCode.REPORT_GENERATION_FAILED.getCode(),
				"보고서 생성 중 오류가 발생했습니다: " + e.getMessage()
			);
		}
	}

	private String buildReport(SystemMetrics metrics, String aiAnalysis) {
		String timestamp = LocalDateTime.now().format(FORMATTER);

		return String.format("""
				━━━━━━━━━━━━━━━━━━━━━
				*서버 모니터링 일일 보고서*
				*보고 시간*: %s
				━━━━━━━━━━━━━━━━━━━━━

				📈 *수집된 메트릭*

				*시스템 리소스*
				• CPU 사용률: %.2f%%
				• 메모리: %dMB / %dMB (%.2f%%)
				• 시스템 가동 시간: %.0f초

				*HTTP 요청 통계*
				• 총 요청 수: %d건
				• 평균 응답 시간: %.2fms
				• 최대 응답 시간: %.2fms

				*데이터베이스 커넥션 풀*
				• 활성 커넥션: %d
				• 유휴 커넥션: %d
				• 최대 커넥션: %d

				🤖 *AI 분석 결과*
				%s

				""",
			timestamp,
			metrics.getCpuUsage(),
			metrics.getMemoryUsed(), metrics.getMemoryMax(), metrics.getMemoryUsagePercent(),
			metrics.getUptimeSeconds(),
			metrics.getHttpRequestCount(),
			metrics.getHttpRequestMeanTime(),
			metrics.getHttpRequestMaxTime(),
			metrics.getDbConnectionActive(),
			metrics.getDbConnectionIdle(),
			metrics.getDbConnectionMax(),
			formatAIAnalysisForSlack(aiAnalysis)
		);
	}

	/**
	 * AI 분석 결과를 Slack mrkdwn 포맷으로 변환
	 */
	private String formatAIAnalysisForSlack(String aiAnalysis) {
		return aiAnalysis
			// **bold** -> *bold*
			.replaceAll("\\*\\*([^*]+)\\*\\*", "*$1*")
			// #### Heading -> *Heading*
			.replaceAll("####\\s+(.+)", "\n*$1*")
			// ### Heading -> *Heading*
			.replaceAll("###\\s+(.+)", "\n*$1*")
			// ## Heading -> *Heading*
			.replaceAll("##\\s+(.+)", "\n*$1*")
			// #숫자. Heading -> *숫자. Heading* (예: #1. 제목)
			.replaceAll("#(\\d+)\\.\\s+(.+)", "\n*$1. $2*")
			// # Heading -> *Heading*
			.replaceAll("#\\s+(.+)", "\n*$1*")
			// 줄바꿈 정리 (연속된 줄바꿈 제거)
			.replaceAll("\n{3,}", "\n\n");
	}

}
