package org.nextme.promotion_service.monitoring.event;

import org.nextme.promotion_service.global.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringEventPublisher {

	private final KafkaTemplate<String, MonitoringNotificationEvent> kafkaTemplate;

	public void publishNotification(MonitoringNotificationEvent event) {
		log.info("Publishing monitoring notification event to kafka");
		kafkaTemplate.send(
			KafkaConfig.MONITORING_NOTIFICATION_TOPIC,
			"monitoring",
			event
		);
	}
}
