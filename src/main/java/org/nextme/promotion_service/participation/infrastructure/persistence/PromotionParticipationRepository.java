package org.nextme.promotion_service.participation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.nextme.promotion_service.participation.domain.ParticipationStatus;
import org.nextme.promotion_service.participation.domain.PromotionParticipation;
import org.nextme.promotion_service.promotion.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionParticipationRepository extends JpaRepository<PromotionParticipation, UUID> {

	// 특정 프로모션의 특정 사용자 참여 기록 조회
	Optional<PromotionParticipation> findByPromotionAndUserId(Promotion promotion, UUID userId);

	// 특정 프로모션의 당첨자 목록 조회 (순번 순)
	List<PromotionParticipation> findByPromotionAndStatusOrderByQueuePositionAsc(
		Promotion promotion, ParticipationStatus status);
}
