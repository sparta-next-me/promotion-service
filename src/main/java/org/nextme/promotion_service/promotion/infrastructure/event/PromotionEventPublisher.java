package org.nextme.promotion_service.promotion.infrastructure.event;

import org.nextme.promotion_service.global.config.RabbitMQConfig;
import org.nextme.promotion_service.promotion.domain.event.PromotionWinnerEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionEventPublisher {

	private final RabbitTemplate rabbitTemplate;

	// 당첨자 이벤트 발행
	public void publishWinnerEvent(PromotionWinnerEvent event) {
		try {
			rabbitTemplate.convertAndSend(
				RabbitMQConfig.PROMOTION_EXCHANGE,
				RabbitMQConfig.WINNER_ROUTING_KEY,
				event
			);
			log.info("당첨 이벤트 발행 성공 - promotionId: {}, userId: {}, position: {}",
				event.getPromotionId(), event.getUserId(), event.getQueuePosition());
		} catch (Exception e) {
			log.error("당첨 이벤트 발행 실패 - promotionId: {}, userId: {}, error: {}",
				event.getPromotionId(), event.getUserId(), e.getMessage(), e);
		}
	}
}
