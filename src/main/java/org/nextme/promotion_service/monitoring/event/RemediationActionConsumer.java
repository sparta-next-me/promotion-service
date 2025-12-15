package org.nextme.promotion_service.monitoring.event;

import org.nextme.promotion_service.monitoring.remediation.RemediationAction;
import org.nextme.promotion_service.monitoring.remediation.RemediationExecutor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Slack 사용자가 모니터링 조치를 승인했을 때 실행하는 Consumer
 * notification-service에서 발행한 Remediation Action 이벤트 수신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemediationActionConsumer {

	private final RemediationExecutor remediationExecutor;

	/**
	 * monitoring.remediation 토픽에서 이벤트 수신
	 * Slack 사용자가 "실행" 버튼을 클릭했을 때 실행
	 */
	@KafkaListener(topics = "monitoring.remediation", groupId = "promotion-service")
	public void handleRemediationAction(RemediationActionEvent event) {
		log.info("Received remediation action approval from user: {} for action: {}",
			event.approvedBy(), event.actionType());

		try {
			// Action type에 따라 RemediationAction 생성
			RemediationAction action = RemediationAction.of(event.actionType());

			if (action != null) {
				// RemediationExecutor 실행
				String result = remediationExecutor.execute(action);
				log.info("Remediation execution result: {}", result);
			} else {
				log.warn("Unknown remediation action type: {}", event.actionType());
			}

		} catch (Exception e) {
			log.error("Failed to execute remediation action: {}", event.actionType(), e);
		}
	}
}
