package org.nextme.promotion_service.monitoring.analyzer;

import org.nextme.infrastructure.exception.ApplicationException;
import org.nextme.promotion_service.monitoring.analyzer.dto.OllamaRequest;
import org.nextme.promotion_service.monitoring.analyzer.dto.OllamaResponse;
import org.nextme.promotion_service.monitoring.exception.MonitoringErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OllamaClient {

	private final RestClient ollamaRestClient;

	@Value("${monitoring.ollama.model}")
	private String model;

	public String generate(String prompt) {
		log.info("Calling Ollama API with model: {}", model);

		try {
			OllamaRequest request = new OllamaRequest(model, prompt, false);

			OllamaResponse response = ollamaRestClient.post()
				.uri("/api/generate")
				.body(request)
				.retrieve()
				.body(OllamaResponse.class);

			if (response == null) {
				log.warn("Ollama returned null response");
				throw new ApplicationException(
					MonitoringErrorCode.OLLAMA_RESPONSE_INVALID.getHttpStatus(),
					MonitoringErrorCode.OLLAMA_RESPONSE_INVALID.getCode(),
					"Ollama가 null 응답을 반환했습니다."
				);
			}

			if (response.getResponse() == null || response.getResponse().isBlank()) {
				log.warn("Ollama returned empty response");
				throw new ApplicationException(
					MonitoringErrorCode.OLLAMA_RESPONSE_INVALID.getHttpStatus(),
					MonitoringErrorCode.OLLAMA_RESPONSE_INVALID.getCode(),
					"Ollama가 빈 응답을 반환했습니다."
				);
			}

			log.info("Ollama response received: {} chars", response.getResponse().length());

			return response.getResponse();

		} catch (ApplicationException e) {
			// 이미 ApplicationException인 경우 그대로 재던지기
			throw e;
		} catch (RestClientException e) {
			log.error("Failed to connect to Ollama API", e);
			throw new ApplicationException(
				MonitoringErrorCode.OLLAMA_CONNECTION_FAILED.getHttpStatus(),
				MonitoringErrorCode.OLLAMA_CONNECTION_FAILED.getCode(),
				"Ollama 서버에 연결할 수 없습니다: " + e.getMessage()
			);
		} catch (Exception e) {
			log.error("Unexpected error while calling Ollama API", e);
			throw new ApplicationException(
				MonitoringErrorCode.OLLAMA_REQUEST_FAILED.getHttpStatus(),
				MonitoringErrorCode.OLLAMA_REQUEST_FAILED.getCode(),
				"Ollama API 호출 중 예상치 못한 오류가 발생했습니다: " + e.getMessage()
			);
		}
	}
}
