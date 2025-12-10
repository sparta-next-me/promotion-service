package org.nextme.promotion_service.monitoring.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OllamaRequest {
	private final String model;
	private final String prompt;
	private final boolean stream;
}