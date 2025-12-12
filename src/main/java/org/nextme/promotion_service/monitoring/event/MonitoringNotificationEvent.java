package org.nextme.promotion_service.monitoring.event;

import java.util.List;

public record MonitoringNotificationEvent(
	List<String> slackUserIds,
	String message
) {
}
