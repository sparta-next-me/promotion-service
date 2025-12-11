package org.nextme.promotion_service.monitoring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GeminiClient {

	private final RestClient restClient;
	private final String apiKey;

	public GeminiClient(
		@Value("${gemini.api-key}") String apiKey,
		RestClient.Builder restClientBuilder
	) {
		this.apiKey = apiKey;
		this.restClient = restClientBuilder
			.baseUrl("https://generativelanguage.googleapis.com/v1beta")
			.build();
	}

	public String generateContent(String prompt) {
		log.debug("Gemini API 호출 시작");

		String requestBody = String.format("""
			{
				"contents": [{
					"parts": [{
						"text": "%s"
					}]
				}],
				"generationConfig": {
					"temperature": 0.3,
					"maxOutputTokens": 8192
				}
			}
			""", prompt.replace("\"", "\\\"").replace("\n", "\\n"));

		try {
			GeminiResponse response = restClient.post()
				.uri("/models/gemini-2.5-flash:generateContent?key=" + apiKey)
				.header("Content-Type", "application/json")
				.body(requestBody)
				.retrieve()
				.body(GeminiResponse.class);

			if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
				String text = response.candidates().get(0).content().parts().get(0).text();
				log.debug("Gemini API 호출 성공");
				return text;
			}

			log.warn("Gemini API 응답이 비어있습니다");
			return "AI 분석을 수행할 수 없습니다.";

		} catch (Exception e) {
			log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
			throw new RuntimeException("Gemini API 호출 실패", e);
		}
	}

	// Response DTOs
	public record GeminiResponse(
		java.util.List<Candidate> candidates
	) {}

	public record Candidate(
		Content content
	) {}

	public record Content(
		java.util.List<Part> parts
	) {}

	public record Part(
		String text
	) {}
}
