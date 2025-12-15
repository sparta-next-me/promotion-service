package org.nextme.promotion_service.monitoring.slack;

import org.nextme.promotion_service.monitoring.remediation.RemediationAction;
import org.nextme.promotion_service.monitoring.remediation.RemediationAction.ActionType;
import org.nextme.promotion_service.monitoring.remediation.RemediationExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Slack 버튼 클릭 callback 처리
 * 사용자가 "Yes" 버튼을 클릭하면 해결방법을 자동 실행
 */
@Slf4j
@RestController
@RequestMapping("/api/monitoring/slack")
@RequiredArgsConstructor
public class SimpleSlackCallback {

	private final RemediationExecutor remediationExecutor;
	private final ObjectMapper objectMapper;

	/**
	 * Slack 인터랙션 callback 처리
	 * 사용자가 버튼을 클릭하면 Slack이 이 엔드포인트로 payload를 전송
	 */
	@PostMapping("/callback")
	public ResponseEntity<String> handleCallback(@RequestBody String payload) {
		log.info("Received Slack callback");

		try {
			JsonNode json = objectMapper.readTree(payload);

			// 1. 요청 타입 확인
			String requestType = json.get("type").asText();
			if (!"block_actions".equals(requestType)) {
				log.warn("Unknown request type: {}", requestType);
				return ResponseEntity.ok("OK");
			}

			// 2. 사용자 정보
			String userId = json.get("user").get("id").asText();
			String userName = json.get("user").get("name").asText();

			// 3. 액션 정보 (버튼 클릭)
			String actionId = json.get("actions").get(0).get("action_id").asText();
			String actionValue = json.get("actions").get(0).get("value").asText();

			log.info("User {} ({}) clicked: {}", userName, userId, actionId);

			// 4. "Yes" 버튼이 아니면 무시
			if (!"yes".equalsIgnoreCase(actionValue)) {
				log.info("User declined action");
				return ResponseEntity.ok("OK");
			}

			// 5. actionId에서 해결방법 추출 (remediation_CLEAR_REDIS_CACHE 형식)
			ActionType actionType = ActionType.valueOf(actionId.replace("remediation_", ""));

			// 6. 해결방법 실행
			RemediationAction action = RemediationAction.builder()
				.type(actionType)
				.description(getActionDescription(actionType))
				.parameters("{}")
				.build();

			String result = remediationExecutor.execute(action);
			log.info("✅ Action executed: {} - Result: {}", actionType, result);

			return ResponseEntity.ok("OK");

		} catch (Exception e) {
			log.error("Failed to handle Slack callback", e);
			return ResponseEntity.ok("OK");  // Slack은 항상 200 OK를 기대함
		}
	}

	private String getActionDescription(ActionType type) {
		return switch (type) {
			case CLEAR_REDIS_CACHE -> "Redis 캐시 초기화";
			case FORCE_GC -> "가비지 컬렉션 실행";
			case ADJUST_DB_POOL -> "DB 풀 정리";
			default -> type.toString();
		};
	}
}
