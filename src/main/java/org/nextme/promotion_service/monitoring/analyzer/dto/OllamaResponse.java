package org.nextme.promotion_service.monitoring.analyzer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OllamaResponse {
	private String model;
	private String response;
	private boolean done;
}
