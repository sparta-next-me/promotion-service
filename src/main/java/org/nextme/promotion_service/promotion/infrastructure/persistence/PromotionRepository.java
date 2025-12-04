package org.nextme.promotion_service.promotion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.nextme.promotion_service.promotion.domain.Promotion;
import org.nextme.promotion_service.promotion.domain.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

	// 특정 상태의 프로모션 조회
	List<Promotion> findByStatus(PromotionStatus status);
}
