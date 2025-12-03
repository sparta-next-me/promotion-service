package org.nextme.promotion_service.promotion.presentation.dto;

import jakarta.validation.constraints.NotNull;

// 프로모션 참여 요청 DTO
public record PromotionJoinRequest(
	@NotNull(message = "사용자 ID는 필수입니다")
	Long userId
) {
}
