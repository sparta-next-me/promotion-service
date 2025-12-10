package org.nextme.promotion_service.monitoring.analyzer;

import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIAnalyzer {

	private final OllamaClient ollamaClient;

	public String analyze(SystemMetrics metrics) {
		log.info("Starting AI analysis for system metrics");

		String prompt = buildPrompt(metrics);
		log.debug("Generated prompt:\n{}", prompt);

		String analysis = ollamaClient.generate(prompt);
		log.info("AI analysis completed: {} characters", analysis.length());

		return analysis;
	}

	private String buildPrompt(SystemMetrics metrics) {
		return String.format("""
				당신은 시스템 모니터링 전문가입니다. 다음 서버 메트릭을 분석하고 한국어로 보고서를 작성해주세요.
				
				## 수집된 메트릭
				- CPU 사용률: %.2f%%
				- 메모리 사용량: %dMB / %dMB (%.2f%%)
				- HTTP 요청 수: %d건
				- HTTP 평균 응답 시간: %.2fms
				- HTTP 최대 응답 시간: %.2fms
				- DB 커넥션 (활성/유휴/최대): %d / %d / %d
				- 시스템 가동 시간: %.0f초
				
				## 작성 지침
				1. **시스템 상태 요약**: 전반적인 서버 상태를 한 문장으로 요약
				2. **주요 지표 분석**: 각 메트릭의 상태 평가 (정상/주의/경고)
				3. **잠재적 문제점**: 현재 트렌드로 볼 때 발생 가능한 문제
				4. **권장 사항**: 성능 개선 또는 유지보수 관련 조언
				
				보고서는 간결하고 실용적으로 작성하되, 기술적 세부사항도 포함해주세요.
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
