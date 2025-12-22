package org.nextme.promotion_service.monitoring.controller;

import org.nextme.promotion_service.monitoring.remediation.RemediationAction;
import org.nextme.promotion_service.monitoring.remediation.RemediationExecutor;
import org.nextme.promotion_service.monitoring.service.MonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 자동 해결 조치 실행 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/promotions/monitoring")
@RequiredArgsConstructor
public class RemediationController {

	private final RemediationExecutor remediationExecutor;
	private final MonitoringService monitoringService;

	/**
	 * 수동으로 AI 분석 보고서 생성 및 Slack 전송
	 * 임계치 상관없이 지금 바로 분석 및 알림 전송
	 *
	 * @return 분석 완료 메시지
	 */
	@PostMapping("/report/manual")
	public ResponseEntity<String> generateManualReport() {
		log.info("Manual report generation requested");

		try {
			monitoringService.generateAndSendReport();
			return ResponseEntity.ok("✅ AI 분석 보고서 생성 완료. Slack으로 전송됩니다.");

		} catch (Exception e) {
			log.error("Failed to generate manual report", e);
			return ResponseEntity.internalServerError()
				.body("❌ 보고서 생성 실패: " + e.getMessage());
		}
	}

	/**
	 * Remediation 액션 실행
	 *
	 * @param actionType 실행할 액션 타입 (예: CLEAR_REDIS_CACHE, FORCE_GC, ADJUST_DB_POOL)
	 * @return 실행 결과 메시지
	 */
	@PostMapping("/remediation/execute")
	public ResponseEntity<RemediationResponse> executeRemediation(
		@RequestParam("actionType") String actionType,
		@RequestParam(value = "approvedBy", required = false) String approvedBy) {

		log.info("Remediation request received - actionType: {}, approvedBy: {}", actionType, approvedBy);

		try {
			// actionType 문자열로부터 RemediationAction 생성
			RemediationAction action = RemediationAction.of(actionType);

			if (action == null) {
				return ResponseEntity.badRequest()
					.body(new RemediationResponse(false, "잘못된 액션 타입: " + actionType));
			}

			// 실행
			String result = remediationExecutor.execute(action);

			log.info("Remediation executed successfully - actionType: {}, result: {}", actionType, result);

			return ResponseEntity.ok(new RemediationResponse(true, result));

		} catch (Exception e) {
			log.error("Remediation execution failed", e);
			return ResponseEntity.internalServerError()
				.body(new RemediationResponse(false, "실행 실패: " + e.getMessage()));
		}
	}

	/**
	 * Remediation 응답 DTO
	 */
	public record RemediationResponse(
		boolean success,
		String message
	) {}
}
