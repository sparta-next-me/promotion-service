package org.nextme.promotion_service.participation.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.nextme.promotion_service.participation.domain.ParticipationStatus;
import org.nextme.promotion_service.participation.domain.PromotionParticipation;

// 참여 결과 응답 DTO
public record ParticipationResultResponse(
	UUID participationId,
	UUID promotionId,
	String promotionName,
	UUID userId,
	ParticipationStatus status,
	Long queuePosition,      // 당첨 순번 (당첨자만)
	LocalDateTime participatedAt,
	String message
) {
	public static ParticipationResultResponse from(PromotionParticipation participation) {
		String message = participation.getStatus() == ParticipationStatus.WON
			? String.format("축하합니다! %d번째 당첨자입니다.", participation.getQueuePosition())
			: "아쉽게도 당첨되지 않았습니다.";

		return new ParticipationResultResponse(
			participation.getId(),
			participation.getPromotion().getId(),
			participation.getPromotion().getName(),
			participation.getUserId(),
			participation.getStatus(),
			participation.getQueuePosition(),
			participation.getParticipatedAt(),
			message
		);
	}
}
