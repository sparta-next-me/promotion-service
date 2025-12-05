package org.nextme.promotion_service.promotion.application;

import java.util.UUID;

import org.nextme.promotion_service.global.exception.PromotionErrorCode;
import org.nextme.promotion_service.promotion.domain.Promotion;
import org.nextme.promotion_service.promotion.domain.PromotionStatus;
import org.nextme.promotion_service.promotion.infrastructure.persistence.PromotionRepository;
import org.nextme.promotion_service.promotion.infrastructure.redis.PromotionQueueService;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionCreateRequest;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionResponse;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionStatusResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

	private final PromotionRepository promotionRepository;
	private final PromotionQueueService queueService;

	/*
	프로모션 생성
	@param request 프로모션 생성 요청
	@return 생성된 프로모션 정보
	 */
	@Transactional
	public PromotionResponse createPromotion(PromotionCreateRequest request) {
		Promotion promotion = Promotion.builder()
			.name(request.name())
			.startTime(request.startTime())
			.endTime(request.endTime())
			.totalStock(request.totalStock())
			.pointAmount(request.pointAmount())
			.status(PromotionStatus.SCHEDULED)
			.build();

		Promotion saved = promotionRepository.save(promotion);
		log.info("프로모션 생성 완료 - id: {}, name: {}", saved.getId(), saved.getName());

		return PromotionResponse.from(saved);
	}

	/*
	프로모션 조회
	@param promotionId 프로모션 ID
	@return 프로모션 정보
	 */
	@Transactional(readOnly = true)
	public PromotionResponse getPromotion(UUID promotionId) {
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(PromotionErrorCode.PROMOTION_NOT_FOUND::toException);

		return PromotionResponse.from(promotion);
	}

	/*
	프로모션 시작
	@param promotionId 프로모션 ID
	@return 시작된 프로모션 정보
	 */
	@Transactional
	public PromotionResponse startPromotion(UUID promotionId) {
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(PromotionErrorCode.PROMOTION_NOT_FOUND::toException);

		promotion.start();
		log.info("프로모션 시작 - id: {}, name: {}", promotion.getId(), promotion.getName());

		return PromotionResponse.from(promotion);
	}

	/*
	프로모션 종료
	@param promotionId 프로모션 ID
	@return 종료된 프로모션 정보
	 */
	@Transactional
	public PromotionResponse endPromotion(UUID promotionId) {
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(PromotionErrorCode.PROMOTION_NOT_FOUND::toException);

		promotion.end();
		log.info("프로모션 종료 - id: {}, name: {}", promotion.getId(), promotion.getName());

		return PromotionResponse.from(promotion);
	}

	/*
	프로모션 참여 현황 조회
	@param promotionId 프로모션 ID
	@return 참여 현황 정보
	 */
	@Transactional(readOnly = true)
	public PromotionStatusResponse getPromotionStatus(UUID promotionId) {
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(PromotionErrorCode.PROMOTION_NOT_FOUND::toException);

		// Redis에서 현황 조회
		Long queueSize = queueService.getQueueSize(promotionId);
		Long participantCount = queueService.getParticipantCount(promotionId);
		Long winnerCount = queueService.getWinnerCount(promotionId);

		return PromotionStatusResponse.of(
			queueSize,
			participantCount,
			winnerCount,
			promotion.getTotalStock()
		);
	}
}
