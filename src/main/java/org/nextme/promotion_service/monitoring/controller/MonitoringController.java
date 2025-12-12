package org.nextme.promotion_service.monitoring.controller;

import org.nextme.promotion_service.monitoring.service.MonitoringService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion")
@ConditionalOnProperty(name = "monitoring.enabled", havingValue = "true")
public class MonitoringController {

	private final MonitoringService monitoringService;

	// 모니터링 보고서 수동 생성 및 전송
	@Operation(summary = "보고서 생성", description = "모니터링 보고서를 수동으로 생성 및 전송합니다.")
	@PostMapping("/monitoring/report")
	public ResponseEntity<String> triggerReport() {
		log.info("Manual monitoring report triggered via API");

		monitoringService.generateAndSendReport();

		return ResponseEntity.accepted().body("Monitoring report generation started");
	}
}
