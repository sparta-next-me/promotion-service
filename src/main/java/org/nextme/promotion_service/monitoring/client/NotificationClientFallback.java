package org.nextme.promotion_service.monitoring.client;

import org.nextme.infrastructure.exception.ApplicationException;
import org.nextme.promotion_service.monitoring.client.dto.SlackUserMessageRequest;
import org.nextme.promotion_service.monitoring.exception.MonitoringErrorCode;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

	@Override
	public void sendToUsers(SlackUserMessageRequest request) {
		log.error("Notification service is unavailable. Fallback activated.");
		log.info("Failed message would be sent to: {}", request.slackUserIds());
		log.info("Message content:\n{}", request.text());

		System.out.println("============================================");
		System.out.println("MONITORING REPORT - Fallback Mode");
		System.out.println("============================================");
		System.out.println(request.text());
		System.out.println("============================================");

		throw new ApplicationException(
			MonitoringErrorCode.NOTIFICATION_SERVICE_UNAVAILABLE.getHttpStatus(),
			MonitoringErrorCode.NOTIFICATION_SERVICE_UNAVAILABLE.getCode(),
			MonitoringErrorCode.NOTIFICATION_SERVICE_UNAVAILABLE.getDefaultMessage()
		);
	}
}
