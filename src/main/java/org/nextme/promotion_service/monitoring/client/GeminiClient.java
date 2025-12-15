package org.nextme.promotion_service.monitoring.client;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI API를 사용한 AI 클라이언트
 * Spring AI의 OpenAiChatModel을 래핑하여 사용
 */
@Slf4j
@Component
public class GeminiClient {

	private final OpenAiChatModel chatModel;
	private final String apiKey;

	public GeminiClient(
		@Value("${spring.ai.openai.api-key:}") String apiKey,
		OpenAiChatModel chatModel
	) {
		this.apiKey = apiKey;
		this.chatModel = chatModel;

		if (apiKey == null || apiKey.trim().isEmpty()) {
			log.warn("⚠️ OPENAI_API_KEY is not set! AI analysis will fail.");
			log.warn("Please set OPENAI_API_KEY environment variable or spring.ai.openai.api-key in application.yaml");
		} else {
			log.info("✅ OpenAI API key loaded successfully (length: {})", apiKey.length());
		}
	}

	public String generateContent(String prompt) {
		log.debug("OpenAI API 호출 시작");

		try {
			String response = chatModel.call(prompt);
			log.debug("OpenAI API 호출 성공");
			return response;

		} catch (Exception e) {
			log.error("OpenAI API 호출 실패: {}", e.getMessage(), e);
			throw new RuntimeException("OpenAI API 호출 실패", e);
		}
	}
}
