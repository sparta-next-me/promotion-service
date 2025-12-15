package org.nextme.promotion_service.monitoring.analyzer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 분석 결과 + 단일 해결방법
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

	/**
	 * 분석 요약
	 * 예: "메모리가 지속적으로 증가하고 있습니다. 현재 추세라면 3시간 후 메모리 부족 예상됩니다."
	 */
	private String analysis;

	/**
	 * 권장 조치
	 * 예: "Redis 캐시 초기화"
	 */
	private String recommendation;

	/**
	 * 권장 근거
	 * 예: "지난 6시간 동안 메모리가 50%에서 90%로 증가했습니다. 캐시 초기화로 즉시 메모리 해제 가능합니다."
	 */
	private String reason;

	/**
	 * 실행 타입 (자동 실행 가능 범위)
	 * CLEAR_REDIS_CACHE, FORCE_GC, ADJUST_DB_POOL 등
	 */
	private String actionType;
}
