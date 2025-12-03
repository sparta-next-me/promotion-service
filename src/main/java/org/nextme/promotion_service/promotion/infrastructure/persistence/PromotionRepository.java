package org.nextme.promotion_service.promotion.infrastructure.persistence;

import java.util.UUID;

import org.nextme.promotion_service.promotion.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
}
