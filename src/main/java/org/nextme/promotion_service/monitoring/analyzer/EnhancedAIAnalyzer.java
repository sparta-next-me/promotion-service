package org.nextme.promotion_service.monitoring.analyzer;

import java.util.List;

import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.nextme.promotion_service.monitoring.history.MetricsHistoryService;
import org.nextme.promotion_service.monitoring.history.MetricsStatistics;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 과거 데이터와 비교하여 예측 분석을 수행하는 AI Analyzer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnhancedAIAnalyzer {

	private final ChatModel chatModel;
	private final MetricsHistoryService metricsHistoryService;

	/**
	 * 현재 메트릭과 과거 24시간 데이터를 비교하여 예측 분석 수행
	 */
	public String analyzeWithHistory(SystemMetrics current) {
		log.info("Starting enhanced AI analysis with historical data");

		// 과거 24시간 메트릭 조회
		List<SystemMetrics> historicalMetrics = metricsHistoryService.getRecentMetrics(24);
		log.info("Retrieved {} historical data points", historicalMetrics.size());

		// 통계 계산
		MetricsStatistics stats = metricsHistoryService.calculateStatistics(historicalMetrics);

		// AI 프롬프트 생성 (과거 데이터 포함)
		String prompt = buildEnhancedPrompt(current, stats);
		log.debug("Generated enhanced prompt:\n{}", prompt);

		// AI 분석 수행
		ChatResponse response = chatModel.call(new Prompt(prompt));
		String analysis = response.getResult().getOutput().getContent();
		log.info("Enhanced AI analysis completed: {} characters", analysis.length());

		return analysis;
	}

	private String buildEnhancedPrompt(SystemMetrics current, MetricsStatistics stats) {
		return String.format("""
				다음 현재 메트릭과 과거 24시간 데이터를 비교 분석하여 예측하세요.

				## 현재 메트릭
				- CPU: %.2f%%
				- 메모리: %.2f%%
				- HTTP 평균 응답: %.2fms
				- DB 커넥션: %d/%d (활성/최대)

				## 과거 24시간 통계 (데이터 포인트: %d개)
				- CPU 평균: %.2f%%, 최대: %.2f%%
				- 메모리 평균: %.2f%%, 최대: %.2f%%
				- 응답시간 평균: %.2fms, 최대: %.2fms

				## 분석 요구사항 (총 500자 이내)
				1. 현재 상태 평가 (1줄)
				2. 과거 대비 변화 트렌드 분석 (2줄)
				3. 예측되는 문제점과 발생 예상 시간 (2줄)
				   예: "현재 추세라면 3시간 후 메모리 부족 예상"
				4. 구체적 해결방법 제시 (3줄)
				   예: "Redis 캐시 초기화", "DB 커넥션 풀 확장", "서버 재시작"

				간결하고 실행 가능한 조치를 명확히 제시하세요.
				""",
			// 현재 메트릭
			current.getCpuUsage(),
			current.getMemoryUsagePercent(),
			current.getHttpRequestMeanTime(),
			current.getDbConnectionActive(),
			current.getDbConnectionMax(),
			// 과거 통계
			stats.getDataPoints(),
			stats.getAvgCpu(),
			stats.getMaxCpu(),
			stats.getAvgMemory(),
			stats.getMaxMemory(),
			stats.getAvgResponseTime(),
			stats.getMaxResponseTime()
		);
	}
}
