package org.nextme.promotion_service.monitoring.controller;

import org.nextme.promotion_service.monitoring.scheduler.MonitoringScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/monitoring")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitoring.enabled", havingValue = "true")
public class MonitoringController {

	private final MonitoringScheduler monitoringScheduler;

	// 모니터링 보고서 수동 생성 및 전송
	@PostMapping("/report")
	public ResponseEntity<String> triggerReport() {
		log.info("Manual monitoring report triggered via API");

		try {
			monitoringScheduler.triggerManualReport();
			return ResponseEntity.ok("Monitoring report generated and sent successfully");
		} catch (Exception e) {
			log.error("Failed to generate monitoring report", e);
			return ResponseEntity.internalServerError().body("Failed to generate report: " + e.getMessage());
		}
	}
}
