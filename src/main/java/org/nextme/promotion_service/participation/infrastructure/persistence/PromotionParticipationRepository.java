package org.nextme.promotion_service.participation.infrastructure.persistence;

import java.util.UUID;

import org.nextme.promotion_service.participation.domain.PromotionParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionParticipationRepository extends JpaRepository<PromotionParticipation, UUID> {
}
