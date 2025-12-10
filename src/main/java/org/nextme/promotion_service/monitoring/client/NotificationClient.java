package org.nextme.promotion_service.monitoring.client;

import org.nextme.promotion_service.monitoring.client.dto.SlackUserMessageRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
	name = "notification-service",
	url = "${monitoring.notification.service-url}",
	fallback = NotificationClientFallback.class
)
public interface NotificationClient {

	@PostMapping("/v1/notifications/slack/users")
	void sendToUsers(@RequestBody SlackUserMessageRequest request);
}
