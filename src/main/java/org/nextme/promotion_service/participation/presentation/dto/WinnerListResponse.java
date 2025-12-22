package org.nextme.promotion_service.participation.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.nextme.promotion_service.participation.domain.PromotionParticipation;

// 당첨자 목록 응답 DTO
public record WinnerListResponse(
	UUID userId,
	Long queuePosition,
	LocalDateTime participatedAt
) {
	public static WinnerListResponse from(PromotionParticipation participation) {
		return new WinnerListResponse(
			participation.getUserId(),
			participation.getQueuePosition(),
			participation.getParticipatedAt()
		);
	}
}
