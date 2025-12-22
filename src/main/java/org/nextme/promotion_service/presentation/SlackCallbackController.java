package org.nextme.promotion_service.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nextme.promotion_service.monitoring.remediation.RemediationAction;
import org.nextme.promotion_service.monitoring.remediation.RemediationExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Slack Interactive 메시지 Callback 처리 Controller
 */
@Slf4j
@RestController
@RequestMapping("/v1/promotions")
@RequiredArgsConstructor
public class SlackCallbackController {

    private final ObjectMapper objectMapper;
    private final RemediationExecutor remediationExecutor;

    /**
     * Slack Interactive 메시지 버튼 클릭 Callback
     * Slack은 application/x-www-form-urlencoded 형식으로 payload를 전송
     */
    @PostMapping("/interactive")
    public ResponseEntity<Void> handleInteractive(@RequestParam("payload") String payload) {
        try {
            log.info("Received Slack interactive callback");

            // JSON 파싱
            JsonNode json = objectMapper.readTree(payload);

            // action_id 추출
            String actionId = json.path("actions").get(0).path("action_id").asText();
            String actionValue = json.path("actions").get(0).path("value").asText();
            String userId = json.path("user").path("id").asText();

            log.info("Slack callback - actionId: {}, actionValue: {}, userId: {}", actionId, actionValue, userId);

            // actionId가 "monitoring_action_approve"이면 실행
            if (actionId.endsWith("_approve")) {
                log.info("User approved remediation action: {}, approvedBy: {}", actionValue, userId);

                // 직접 remediation 실행
                RemediationAction action = RemediationAction.of(actionValue);
                if (action != null) {
                    String result = remediationExecutor.execute(action);
                    log.info("Remediation executed - result: {}", result);
                } else {
                    log.warn("Invalid remediation action type: {}", actionValue);
                }

            } else if (actionId.endsWith("_reject")) {
                log.info("User rejected remediation action: {}", actionValue);
            }

            // Slack은 3초 내에 200 OK 응답을 기대
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Failed to process Slack interactive callback", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
