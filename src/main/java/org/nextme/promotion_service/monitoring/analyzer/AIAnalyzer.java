package org.nextme.promotion_service.monitoring.analyzer;

import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIAnalyzer {

	private final ChatModel chatModel;

	public String analyze(SystemMetrics metrics) {
		log.info("Starting AI analysis for system metrics");

		String prompt = buildPrompt(metrics);
		log.debug("Generated prompt:\n{}", prompt);

		ChatResponse response = chatModel.call(new Prompt(prompt));
		String analysis = response.getResult().getOutput().getContent();
		log.info("AI analysis completed: {} characters", analysis.length());

		return analysis;
	}

	private String buildPrompt(SystemMetrics metrics) {
		return String.format("""
				다음 서버 메트릭을 분석하고 간결하게 보고하세요.

				## 메트릭
				- CPU: %.2f%%
				- 메모리: %dMB / %dMB (%.2f%%)
				- HTTP 요청: %d건, 평균 %.2fms, 최대 %.2fms
				- DB 커넥션: %d / %d / %d (활성/유휴/최대)
				- 가동시간: %.0f초

				## 작성 요구사항 (총 300자 이내)
				1. 상태 요약 (1줄)
				2. 주요 지표 평가 (정상/주의/경고, 3줄 이내)
				3. 권장사항 (2줄 이내)

				간결하고 핵심만 작성하세요.
				""",
			metrics.getCpuUsage(),
			metrics.getMemoryUsed(), metrics.getMemoryMax(), metrics.getMemoryUsagePercent(),
			metrics.getHttpRequestCount(),
			metrics.getHttpRequestMeanTime(),
			metrics.getHttpRequestMaxTime(),
			metrics.getDbConnectionActive(), metrics.getDbConnectionIdle(),
			metrics.getDbConnectionMax(),
			metrics.getUptimeSeconds()
		);
	}

}
