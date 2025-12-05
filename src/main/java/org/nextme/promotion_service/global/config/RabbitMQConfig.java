package org.nextme.promotion_service.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	// Exchange 이름
	public static final String PROMOTION_EXCHANGE = "promotion.exchange";

	// Queue 이름
	public static final String WINNER_QUEUE = "point.promotion.winner";

	// Routing Key
	public static final String WINNER_ROUTING_KEY = "promotion.winner";

	// Exchange 생성 (Topic)
	@Bean
	public TopicExchange promotionExchange() {
		return new TopicExchange(PROMOTION_EXCHANGE);
	}

	// Queue 생성 (당첨자 큐)
	@Bean
	public Queue winnerQueue() {
		return new Queue(WINNER_QUEUE, true);
	}

	// Binding (Exchange - Queue - RoutingKey 연결)
	@Bean
	public Binding winnerBinding(Queue winnerQueue, TopicExchange promotionExchange) {
		return BindingBuilder
			.bind(winnerQueue)
			.to(promotionExchange)
			.with(WINNER_ROUTING_KEY);
	}

	// RabbitTemplate - JSON 메시지 컨버터 설정
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
		return rabbitTemplate;
	}

	// Jackson2JsonMessageConverter - 객체를 JSON으로 변환
	@Bean
	public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
