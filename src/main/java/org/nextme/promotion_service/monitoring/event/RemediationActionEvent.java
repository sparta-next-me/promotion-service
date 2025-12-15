package org.nextme.promotion_service.monitoring.event;

/**
 * Remediation Action 이벤트
 * notification-service에서 Slack 사용자 승인 시 발행
 */
public record RemediationActionEvent(
	String actionType,
	String approvedBy
) {
}
