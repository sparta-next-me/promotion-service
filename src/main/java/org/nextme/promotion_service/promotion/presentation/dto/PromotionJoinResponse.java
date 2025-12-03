package org.nextme.promotion_service.promotion.presentation.dto;

// 프로모션 참여 응답 DTO
public record PromotionJoinResponse(
	boolean success,	// 성공 여부
	String message,		// 응답 메시지
	Long queuePosition
) {
	// 성공 응답 생성
	public static PromotionJoinResponse success(String message, Long queuePosition) {
		return new PromotionJoinResponse(true, message, queuePosition);
	}

	// 실패 응답 생성
	public static PromotionJoinResponse failure(String message) {
		return new PromotionJoinResponse(false, message, null);
	}
}
