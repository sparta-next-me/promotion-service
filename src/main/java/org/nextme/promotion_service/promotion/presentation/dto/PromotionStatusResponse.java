package org.nextme.promotion_service.promotion.presentation.dto;

// 프로모션 참여 현황 응답 DTO
public record PromotionStatusResponse(
	Long queueSize,        // 현재 대기열 크기
	Long participantCount, // 총 참여자 수 (joined set)
	Long winnerCount,      // 현재 당첨자 수
	Integer totalStock,    // 총 재고
	Integer remainingStock // 남은 재고
) {
	public static PromotionStatusResponse of(
		Long queueSize,
		Long participantCount,
		Long winnerCount,
		Integer totalStock
	) {
		int remaining = Math.max(0, totalStock - (winnerCount != null ? winnerCount.intValue() : 0));
		return new PromotionStatusResponse(
			queueSize,
			participantCount,
			winnerCount,
			totalStock,
			remaining
		);
	}
}
