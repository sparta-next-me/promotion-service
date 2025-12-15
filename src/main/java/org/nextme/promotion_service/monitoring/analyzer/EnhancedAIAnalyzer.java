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
 * 과거 데이터와 비교하여 분석 + 단일 해결방법 제시
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnhancedAIAnalyzer {

	private final ChatModel chatModel;
	private final MetricsHistoryService metricsHistoryService;

	/**
	 * 현재 메트릭과 과거 데이터를 비교하여 분석 + 단일 해결방법 제시
	 */
	public AnalysisResult analyzeWithHistory(SystemMetrics current) {
		log.info("Starting analysis with historical data comparison");

		try {
			// 과거 6시간 메트릭 조회
			List<SystemMetrics> historicalMetrics = metricsHistoryService.getRecentMetrics(6);
			log.info("Retrieved {} historical data points", historicalMetrics.size());

			// 통계 계산
			MetricsStatistics stats = metricsHistoryService.calculateStatistics(historicalMetrics);

			// AI 프롬프트 생성
			String prompt = buildPrompt(current, stats);
			log.debug("Generated prompt:\n{}", prompt);

			// AI 분석 수행
			ChatResponse response = chatModel.call(new Prompt(prompt));
			String aiResponse = response.getResult().getOutput().getContent();
			log.info("AI analysis completed");

			// 응답 파싱 (분석 + 해결방법 추출)
			return parseAnalysisResponse(aiResponse, stats, current);

		} catch (Exception e) {
			log.error("Failed to analyze metrics", e);
			// Fallback: 기본 권장사항 반환
			return buildFallbackAnalysis(current);
		}
	}

	private String buildPrompt(SystemMetrics current, MetricsStatistics stats) {
		return String.format("""
				당신은 서버 모니터링 시스템의 AI 분석가입니다. 다음 정보를 분석하여 상세한 진단을 제공하세요.

				## 📊 현재 서버 메트릭 (실시간)
				- CPU 사용률: %.2f%% (임계치: 80%%)
				- 메모리 사용률: %.2f%% (임계치: 85%%)
				- HTTP 평균 응답시간: %.2fms (임계치: 5ms)
				- DB 활성 커넥션: %d/%d개 (사용률: %.1f%%)

				## 📈 과거 6시간 트렌드 분석 (데이터 포인트: %d개)
				- 메모리: 평균 %.2f%%, 최대 %.2f%%, 최소값부터 최대값까지 변동
				- CPU: 평균 %.2f%%, 최대 %.2f%%
				- 추세: 증가/감소/안정적인 패턴 분석

				## 🔍 분석 요청사항
				1. **현재 상태 진단**: 현재 메트릭이 과거 데이터와 비교했을 때 어떤 상태인지 분석
				2. **문제 식별**: 임계치 초과 부분과 잠재적 문제점 지적
				3. **예측 및 전망**: 현재 추세가 계속되면 얼마 후 문제가 심화될 것인지 (예: "약 2시간 후 심각한 상태 예상")
				4. **근본 원인 추측**: 문제의 원인이 무엇일 수 있는지
				5. **해결방법 제시**: 다음 중 가장 적절한 조치 1가지만 선택:
				   - "Redis 캐시 초기화": 메모리 사용량이 지속적으로 증가할 때, 캐시로 인한 메모리 누적 의심
				   - "가비지 컬렉션 실행": 메모리 누수 의심 또는 비효율적 메모리 관리 시
				   - "DB 커넥션 풀 정리": DB 커넥션 사용률이 높을 때, 유휴 연결 정리 필요
				6. **해결방법 선택 근거**: 왜 그 방법을 선택했는지 명확하게 설명

				## 📝 응답 형식 (필수)
				[분석]: (현재 상태, 문제점, 예측을 포함한 3-4줄 종합 분석)
				[권장]: (해결방법명 - 위 3가지 중 정확히 하나)
				[근거]: (해결방법을 선택한 이유 - 2-3줄)

				분석을 상세하고 구체적으로 작성하되, 시간 예측과 수치 기반 진단을 포함하세요.
				""",
			current.getCpuUsage(),
			current.getMemoryUsagePercent(),
			current.getHttpRequestMeanTime(),
			current.getDbConnectionActive(),
			current.getDbConnectionMax(),
			(double) current.getDbConnectionActive() / current.getDbConnectionMax() * 100,
			stats.getDataPoints(),
			stats.getAvgMemory(),
			stats.getMaxMemory(),
			stats.getAvgCpu(),
			stats.getMaxCpu()
		);
	}

	private AnalysisResult parseAnalysisResponse(String aiResponse, MetricsStatistics stats, SystemMetrics current) {
		log.info("Parsing AI response");

		String analysis = "";
		String recommendation = "";
		String reason = "";
		String actionType = "";

		try {
			// 간단한 파싱
			String[] parts = aiResponse.split("\\[");

			for (String part : parts) {
				if (part.startsWith("분석]")) {
					analysis = part.replace("분석]", "").split("\\[")[0].trim();
				} else if (part.startsWith("권장]")) {
					recommendation = part.replace("권장]", "").split("\\[")[0].trim();
					actionType = mapRecommendationToActionType(recommendation);
				} else if (part.startsWith("근거]")) {
					reason = part.replace("근거]", "").trim();
				}
			}

			// 파싱 실패 시 fallback
			if (recommendation.isEmpty()) {
				return buildFallbackAnalysis(current);
			}

			return AnalysisResult.builder()
				.analysis(analysis)
				.recommendation(recommendation)
				.reason(reason)
				.actionType(actionType)
				.build();

		} catch (Exception e) {
			log.warn("Failed to parse AI response, using fallback", e);
			return buildFallbackAnalysis(current);
		}
	}

	private String mapRecommendationToActionType(String recommendation) {
		if (recommendation.contains("Redis") || recommendation.contains("캐시")) {
			return "CLEAR_REDIS_CACHE";
		} else if (recommendation.contains("GC") || recommendation.contains("가비지")) {
			return "FORCE_GC";
		} else if (recommendation.contains("DB") || recommendation.contains("커넥션")) {
			return "ADJUST_DB_POOL";
		}
		return "CLEAR_REDIS_CACHE";  // 기본값
	}

	private AnalysisResult buildFallbackAnalysis(SystemMetrics current) {
		log.warn("⚠️ Using fallback analysis (AI unavailable)");

		String analysis = "";
		String recommendation = "";
		String reason = "";
		String actionType = "";

		// CPU 초과
		if (current.getCpuUsage() > 80) {
			analysis = "CPU 사용량이 정상 범위를 초과했습니다. 현재 CPU가 " + String.format("%.1f", current.getCpuUsage()) + "%로 높은 상태입니다.";
			recommendation = "가비지 컬렉션 실행";
			actionType = "FORCE_GC";
			reason = "CPU 부하 증가 시 가비지 컬렉션을 실행하여 메모리 정리 및 CPU 부하 감소를 시도합니다.";
		}
		// 메모리 초과
		else if (current.getMemoryUsagePercent() > 85) {
			analysis = "메모리 사용량이 정상 범위를 크게 초과했습니다. 현재 메모리가 " + String.format("%.1f", current.getMemoryUsagePercent()) + "%로 위험 상태입니다.";
			recommendation = "가비지 컬렉션 실행";
			actionType = "FORCE_GC";
			reason = "높은 메모리 사용량을 감소시키기 위해 가비지 컬렉션을 실행합니다.";
		}
		// DB 커넥션 초과
		else if (current.getDbConnectionActive() > current.getDbConnectionMax() * 0.9) {
			analysis = "DB 커넥션 사용률이 높습니다. 현재 활성 커넥션이 " + current.getDbConnectionActive() + "/" + current.getDbConnectionMax() + "입니다.";
			recommendation = "DB 풀 정리";
			actionType = "ADJUST_DB_POOL";
			reason = "활성 DB 커넥션이 많아서 풀을 정리하여 유휴 커넥션을 제거합니다.";
		}
		// 응답시간 초과
		else if (current.getHttpRequestMeanTime() > 1000) {
			analysis = "HTTP 응답시간이 높습니다. 현재 평균 응답시간이 " + String.format("%.0f", current.getHttpRequestMeanTime()) + "ms입니다.";
			recommendation = "Redis 캐시 초기화";
			actionType = "CLEAR_REDIS_CACHE";
			reason = "응답시간 개선을 위해 Redis 캐시를 초기화하여 신선한 데이터로 갱신합니다.";
		}
		// 기본값
		else {
			analysis = "임계치를 초과한 메트릭이 감지되었습니다. 즉시 조치가 필요합니다.";
			recommendation = "Redis 캐시 초기화";
			actionType = "CLEAR_REDIS_CACHE";
			reason = "캐시 초기화를 통해 메모리 공간을 확보하고 리소스를 정리합니다.";
		}

		return AnalysisResult.builder()
			.analysis(analysis)
			.recommendation(recommendation)
			.reason(reason)
			.actionType(actionType)
			.build();
	}
}
