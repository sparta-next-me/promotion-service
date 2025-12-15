package org.nextme.promotion_service.monitoring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

/**
 * 모니터링 설정
 * - Spring AI의 OpenAiChatModel을 자동으로 빈으로 등록 (spring-ai-openai-spring-boot-starter)
 * - GeminiClient는 OpenAiChatModel을 주입받아서 사용
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableAsync
public class MonitoringConfig {
	// OpenAiChatModel은 Spring Boot AutoConfiguration으로 자동 등록됨
}

