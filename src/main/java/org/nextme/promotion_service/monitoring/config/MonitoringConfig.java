package org.nextme.promotion_service.monitoring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@Configuration
@EnableFeignClients(basePackages = "org.nextme.promotion_service.monitoring")
@EnableScheduling
public class MonitoringConfig {

	@Bean
	public RestClient ollamaRestClient(@Value("${monitoring.ollama.url}") String ollamaUrl) {
		return RestClient.builder()
			.baseUrl(ollamaUrl)
			.build();
	}
}
