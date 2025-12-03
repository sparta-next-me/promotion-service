package org.nextme.promotion_service.global.exception;

import org.nextme.infrastructure.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionErrorCode {

	// 프로모션 관련
	PROMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PROMOTION_NOT_FOUND", "존재하지 않는 프로모션입니다"),
	PROMOTION_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "PROMOTION_NOT_AVAILABLE", "참여 가능한 프로모션이 아닙니다."),
	PROMOTION_ALREADY_JOINED(HttpStatus.CONFLICT, "PROMOTION_ALREADY_JOINED", "이미 참여하셨습니다"),
	PROMOTION_QUEUE_FULL(HttpStatus.TOO_MANY_REQUESTS, "PROMOTION_QUEUE_FULL", "대기 인원이 초과되었습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;

	// ApplicationException 생성 헬퍼
	public ApplicationException toException() {
		return new ApplicationException(this.httpStatus, this.code, this.defaultMessage);
	}

	// 커스텀 메시지로 ApplicationException 발생
	public ApplicationException toException(String customMessage) {
		return new ApplicationException(this.httpStatus, this.code, customMessage);
	}

}
