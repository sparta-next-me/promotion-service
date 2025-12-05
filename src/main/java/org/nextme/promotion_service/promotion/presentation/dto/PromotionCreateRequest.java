package org.nextme.promotion_service.promotion.presentation.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 프로모션 생성 요청 DTO
public record PromotionCreateRequest(
	@NotBlank(message = "프로모션 이름은 필수입니다")
	String name,

	@NotNull(message = "시작 시간은 필수입니다")
	LocalDateTime startTime,

	@NotNull(message = "종료 시간은 필수입니다")
	LocalDateTime endTime,

	@NotNull(message = "총 재고는 필수입니다")
	@Min(value = 1, message = "총 재고는 1 이상이어야 합니다")
	Integer totalStock,

	@NotNull(message = "포인트 금액은 필수입니다")
	@Min(value = 0, message = "포인트 금액은 0 이상이어야 합니다")
	Integer pointAmount
) {
}
