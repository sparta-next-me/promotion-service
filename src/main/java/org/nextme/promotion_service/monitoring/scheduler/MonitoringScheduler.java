package org.nextme.promotion_service.monitoring.scheduler;

import org.nextme.promotion_service.monitoring.service.MonitoringService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitoring.enabled", havingValue = "true")
public class MonitoringScheduler {

	private final MonitoringService monitoringService;

	/**
	 * 매일 정해진 시간에 모니터링 보고서를 생성하고 전송합니다.
	 */
	@Scheduled(cron = "${monitoring.schedule.daily-report}")
	public void scheduleDailyReport() {
		log.info("Scheduled daily monitoring report triggered");
		monitoringService.generateAndSendReport();
	}
}
