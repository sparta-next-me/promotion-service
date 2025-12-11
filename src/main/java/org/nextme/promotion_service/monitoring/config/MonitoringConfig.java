package org.nextme.promotion_service.monitoring.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableFeignClients(basePackages = "org.nextme.promotion_service.monitoring")
@EnableScheduling
public class MonitoringConfig {

	@Bean
	@Primary
	@ConditionalOnProperty(name = "monitoring.ai.provider", havingValue = "gemini")
	public ChatModel geminiChatModel(VertexAiGeminiChatModel vertexAiGeminiChatModel) {
		log.info("Using Gemini AI provider");
		return vertexAiGeminiChatModel;
	}

	@Bean
	@ConditionalOnProperty(name = "monitoring.ai.provider", havingValue = "ollama")
	public ChatModel ollamaChatModel(OllamaChatModel ollamaChatModel) {
		log.info("Using Ollama AI provider");
		return ollamaChatModel;
	}
}
