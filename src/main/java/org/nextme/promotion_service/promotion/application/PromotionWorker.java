package org.nextme.promotion_service.promotion.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.nextme.promotion_service.participation.domain.PromotionParticipation;
import org.nextme.promotion_service.participation.infrastructure.persistence.PromotionParticipationRepository;
import org.nextme.promotion_service.promotion.domain.Promotion;
import org.nextme.promotion_service.promotion.domain.PromotionStatus;
import org.nextme.promotion_service.promotion.infrastructure.persistence.PromotionRepository;
import org.nextme.promotion_service.promotion.infrastructure.redis.PromotionQueueService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
프로모션 대기열 처리 워커
주기적으로 Redis 대기열에서 데이터를 꺼내 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionWorker {

	private final PromotionQueueService queueService;
	private final PromotionRepository promotionRepository;
	private final PromotionParticipationRepository participationRepository;

	// 배치 크기
	private static final int BATCH_SIZE = 100;

	// 대기열 처리 스케줄러
	@Scheduled(fixedRate = 1000)
	@Transactional
	public void processQueue() {
		// ACTIVE 상태의 모든 프로모션 조회
		List<Promotion> activePromotions = promotionRepository.findByStatus(PromotionStatus.ACTIVE);

		if (activePromotions.isEmpty()) {
			return;
		}

		log.debug("활성 프로모션 {} 개 처리 시작", activePromotions.size());

		// 각 프로모션의 큐 처리
		for (Promotion promotion : activePromotions) {
			try {
				Long queueSize = queueService.getQueueSize(promotion.getId());
				if (queueSize > 0) {
					log.debug("프로모션 {} 큐 처리 - 대기 {}명", promotion.getName(), queueSize);
					processPromotionQueue(promotion.getId());
				}
			} catch (Exception e) {
				log.error("프로모션 큐 처리 중 오류 - promotionId: {}, error: {}", promotion.getId(), e.getMessage(), e);
			}
		}
	}

	public void processPromotionQueue(UUID promotionId) {
		// 1. 프로모션 조회
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElse(null);

		if (promotion == null) {
			log.warn("프로모션을 찾을 수 없음 - promotionId: {}", promotionId);
			return;
		}

		// 2. 배치 처리
		List<PromotionParticipation> batch = new ArrayList<>();

		for (int i = 0; i < BATCH_SIZE; i++) {
			// 큐에서 데이터 꺼내기
			String queueData = queueService.dequeue(promotionId);

			if (queueData == null) {
				break; // 큐가 비었으면 종료
			}

			// 데이터 파싱 : "userId:ipAddress:timestamp"
			String[] parts = queueData.split(":");
			if (parts.length < 3) {
				log.warn("잘못된 큐 데이터 형식 - data: {}", queueData);
				continue;
			}

			Long userId = Long.parseLong(parts[0]);
			String ipAddress = parts[1];
			String userAgent = parts[2];

			// 선착순 판단 (원자적 증가)
			Long winnerCount = queueService.incrementWinnerCount(promotionId);

			// 당첨/탈락 결정
			PromotionParticipation participation;
			if (winnerCount <= promotion.getTotalStock()) {
				// 당첨
				participation = PromotionParticipation.createWinner(promotion, userId, ipAddress, userAgent, winnerCount);
				log.info("당첨 - promotionId: {}, userId: {}, position: {}", promotionId, userId, winnerCount);
			} else {
				// 탈락
				participation = PromotionParticipation.createLoser(promotion, userId, ipAddress, userAgent);
				log.info("탈락 - promotionId: {}, userId: {}", promotionId, userId);
			}

			batch.add(participation); // 배치에 추가
		}

		// 3. 배치 저장
		if (!batch.isEmpty()) {
			participationRepository.saveAll(batch);
			log.info("배치 저장 완료 - promotionId: {}, count: {}", promotionId, batch.size());
		}
	}
}
