package org.nextme.promotion_service.monitoring.client.dto;

import java.util.List;

public record SlackUserMessageRequest(
	List<String> slackUserIds,
	String text
) {
}
