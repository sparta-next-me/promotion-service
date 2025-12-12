package org.nextme.promotion_service.monitoring.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MonitoringErrorCode {

	// AI 관련
	AI_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI_CONNECTION_FAILED", "AI 서버에 연결할 수 없습니다."),
	AI_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_REQUEST_FAILED", "AI API 요청이 실패했습니다."),
	AI_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "AI_RESPONSE_INVALID", "AI 응답이 올바르지 않습니다."),

	// Notification Service 관련
	NOTIFICATION_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "NOTIFICATION_SERVICE_UNAVAILABLE", "알림 서비스를 사용할 수 없습니다."),
	NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION_SEND_FAILED", "알림 전송에 실패했습니다."),

	// 모니터링 일반
	METRICS_COLLECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "METRICS_COLLECTION_FAILED", "메트릭 수집에 실패했습니다."),
	REPORT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT_GENERATION_FAILED", "보고서 생성에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;
}
