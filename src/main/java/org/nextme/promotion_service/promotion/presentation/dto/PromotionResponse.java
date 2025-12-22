package org.nextme.promotion_service.promotion.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.nextme.promotion_service.promotion.domain.Promotion;
import org.nextme.promotion_service.promotion.domain.PromotionStatus;

// 프로모션 응답 DTO
public record PromotionResponse(
	UUID id,
	String name,
	LocalDateTime startTime,
	LocalDateTime endTime,
	Integer totalStock,
	Integer pointAmount,
	PromotionStatus status,
	String etc,
	LocalDateTime createdAt
) {
	public static PromotionResponse from(Promotion promotion) {
		return new PromotionResponse(
			promotion.getId(),
			promotion.getName(),
			promotion.getStartTime(),
			promotion.getEndTime(),
			promotion.getTotalStock(),
			promotion.getPointAmount(),
			promotion.getStatus(),
			promotion.getEtc(),
			promotion.getCreatedAt()
		);
	}
}
