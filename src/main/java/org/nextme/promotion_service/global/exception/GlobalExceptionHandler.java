package org.nextme.promotion_service.global.exception;

import org.nextme.infrastructure.exception.ErrorCode;
import org.nextme.infrastructure.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse<Void>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException e) {
		log.warn("HttpMessageNotReadableException: {}", e.getMessage());

		ErrorCode errorCode = ErrorCode.REQUEST_VALIDATION_ERROR;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(new ErrorResponse<>(errorCode.getCode(), "요청 본문이 누락되었거나 형식이 올바르지 않습니다.", null));
	}
}
