package org.nextme.promotion_service.global.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

	// Topic 이름
	public static final String PROMOTION_WINNER_TOPIC = "promotion.winner";
	public static final String MONITORING_NOTIFICATION_TOPIC = "monitoring.notification";
}
