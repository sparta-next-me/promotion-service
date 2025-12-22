package org.nextme.promotion_service.monitoring.detector;

import org.nextme.promotion_service.monitoring.collector.dto.SystemMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 메트릭 이상 감지기
 * 설정된 임계치를 초과하는지 체크
 */
@Slf4j
@Component
public class AnomalyDetector {

	@Value("${monitoring.anomaly-detection.thresholds.cpu-usage:80.0}")
	private double cpuThreshold;

	@Value("${monitoring.anomaly-detection.thresholds.memory-usage:85.0}")
	private double memoryThreshold;

	@Value("${monitoring.anomaly-detection.thresholds.response-time:1000.0}")
	private double responseTimeThreshold;

	@Value("${monitoring.anomaly-detection.thresholds.db-connection:90.0}")
	private double dbConnectionThreshold;

	/**
	 * 이상 감지 여부 확인
	 *
	 * @param metrics 현재 메트릭
	 * @return 이상 감지 시 true
	 */
	public boolean isAnomalyDetected(SystemMetrics metrics) {
		boolean isCpuAnomaly = metrics.getCpuUsage() > cpuThreshold;
		boolean isMemoryAnomaly = metrics.getMemoryUsagePercent() > memoryThreshold;
		boolean isResponseTimeAnomaly = metrics.getHttpRequestMeanTime() > responseTimeThreshold;
		boolean isDbConnectionAnomaly = isDbConnectionOverThreshold(metrics);

		if (isCpuAnomaly) {
			log.warn("CPU anomaly detected: {}% (threshold: {}%)",
				metrics.getCpuUsage(), cpuThreshold);
		}

		if (isMemoryAnomaly) {
			log.warn("Memory anomaly detected: {}% (threshold: {}%)",
				metrics.getMemoryUsagePercent(), memoryThreshold);
		}

		if (isResponseTimeAnomaly) {
			log.warn("Response time anomaly detected: {}ms (threshold: {}ms)",
				metrics.getHttpRequestMeanTime(), responseTimeThreshold);
		}

		if (isDbConnectionAnomaly) {
			log.warn("DB connection anomaly detected: {}/{} (threshold: {}%)",
				metrics.getDbConnectionActive(), metrics.getDbConnectionMax(), dbConnectionThreshold);
		}

		return isCpuAnomaly || isMemoryAnomaly || isResponseTimeAnomaly || isDbConnectionAnomaly;
	}

	/**
	 * DB 커넥션 사용률이 임계치를 초과하는지 확인
	 */
	private boolean isDbConnectionOverThreshold(SystemMetrics metrics) {
		if (metrics.getDbConnectionMax() == 0) {
			return false;
		}

		double connectionUsagePercent = (double) metrics.getDbConnectionActive()
			/ metrics.getDbConnectionMax() * 100;

		return connectionUsagePercent > dbConnectionThreshold;
	}

	/**
	 * 감지된 이상 유형 설명
	 */
	public String getAnomalyDescription(SystemMetrics metrics) {
		StringBuilder description = new StringBuilder("🚨 *이상 감지*\n");

		if (metrics.getCpuUsage() > cpuThreshold) {
			description.append(String.format("• CPU: %.2f%% (임계치: %.2f%%)\n",
				metrics.getCpuUsage(), cpuThreshold));
		}

		if (metrics.getMemoryUsagePercent() > memoryThreshold) {
			description.append(String.format("• 메모리: %.2f%% (임계치: %.2f%%)\n",
				metrics.getMemoryUsagePercent(), memoryThreshold));
		}

		if (metrics.getHttpRequestMeanTime() > responseTimeThreshold) {
			description.append(String.format("• 응답시간: %.2fms (임계치: %.2fms)\n",
				metrics.getHttpRequestMeanTime(), responseTimeThreshold));
		}

		if (isDbConnectionOverThreshold(metrics)) {
			double usagePercent = (double) metrics.getDbConnectionActive()
				/ metrics.getDbConnectionMax() * 100;
			description.append(String.format("• DB 커넥션: %d/%d (%.2f%%, 임계치: %.2f%%)\n",
				metrics.getDbConnectionActive(), metrics.getDbConnectionMax(),
				usagePercent, dbConnectionThreshold));
		}

		return description.toString();
	}
}
