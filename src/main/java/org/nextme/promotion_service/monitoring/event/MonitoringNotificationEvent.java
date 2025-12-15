package org.nextme.promotion_service.monitoring.event;

import java.util.List;

public record MonitoringNotificationEvent(
	List<String> slackUserIds,
	String message,
	String actionId,
	String actionValue
) {
	/**
	 * 일반 메시지 생성자 (버튼 없음)
	 */
	public MonitoringNotificationEvent(List<String> slackUserIds, String message) {
		this(slackUserIds, message, null, null);
	}
}
