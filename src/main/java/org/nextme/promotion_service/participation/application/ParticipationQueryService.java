package org.nextme.promotion_service.participation.application;

import java.util.List;
import java.util.UUID;

import org.nextme.promotion_service.global.exception.PromotionErrorCode;
import org.nextme.promotion_service.participation.domain.ParticipationStatus;
import org.nextme.promotion_service.participation.domain.PromotionParticipation;
import org.nextme.promotion_service.participation.infrastructure.persistence.PromotionParticipationRepository;
import org.nextme.promotion_service.participation.presentation.dto.ParticipationResultResponse;
import org.nextme.promotion_service.participation.presentation.dto.WinnerListResponse;
import org.nextme.promotion_service.promotion.domain.Promotion;
import org.nextme.promotion_service.promotion.infrastructure.persistence.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationQueryService {

	private final PromotionRepository promotionRepository;
	private final PromotionParticipationRepository participationRepository;

	/*
	특정 사용자의 참여 결과 조회
	@param promotionId 프로모션 ID
	@param userId 사용자 ID
	@return 참여 결과
	 */
	@Transactional(readOnly = true)
	public ParticipationResultResponse getParticipationResult(UUID promotionId, Long userId) {
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(PromotionErrorCode.PROMOTION_NOT_FOUND::toException);

		PromotionParticipation participation = participationRepository
			.findByPromotionAndUserId(promotion, userId)
			.orElseThrow(PromotionErrorCode.PARTICIPATION_NOT_FOUND::toException);

		return ParticipationResultResponse.from(participation);
	}

	/*
	당첨자 목록 조회
	@param promotionId 프로모션 ID
	@return 당첨자 목록
	 */
	@Transactional(readOnly = true)
	public List<WinnerListResponse> getWinners(UUID promotionId) {
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(PromotionErrorCode.PROMOTION_NOT_FOUND::toException);

		List<PromotionParticipation> winners = participationRepository
			.findByPromotionAndStatusOrderByQueuePositionAsc(promotion, ParticipationStatus.WON);

		return winners.stream()
			.map(WinnerListResponse::from)
			.toList();
	}
}
