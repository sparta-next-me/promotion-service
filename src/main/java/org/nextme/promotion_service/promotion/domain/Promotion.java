package org.nextme.promotion_service.promotion.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.nextme.common.jpa.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_promotion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Promotion extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	// 프로모션 이름
	@Column(nullable = false)
	private String name;

	// 프로모션 시작 시간
	@Column(nullable = false)
	private LocalDateTime startTime;

	// 프로모션 종료 시간
	@Column(nullable = false)
	private LocalDateTime endTime;

	// 선착순 당첨 인원
	@Column(nullable = false)
	private Integer totalStock;

	// 지급할 포인트 금액
	@Column(nullable = false)
	private Integer pointAmount;

	// 프로모션 상태
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PromotionStatus status;

	@Builder
	public Promotion(String name, LocalDateTime startTime, LocalDateTime endTime,
		Integer totalStock, Integer pointAmount, PromotionStatus status) {
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.totalStock = totalStock;
		this.pointAmount = pointAmount;
		this.status = status != null ? status : PromotionStatus.SCHEDULED;
	}

	// 현재 프로모션이 진행 중인지 확인
	public boolean isActive() {
		LocalDateTime now = LocalDateTime.now();
		return status == PromotionStatus.ACTIVE
			&& now.isAfter(startTime)
			&& now.isBefore(endTime);
	}

	// 참여 가능한 상태인지 검증
	public boolean canParticipate() {
		return isActive() && totalStock > 0;
	}

	// 프로모션 시작 처리
	public void start() {
		this.status = PromotionStatus.ACTIVE;
	}

	// 프로모션 종료 처리
	public void end() {
		this.status = PromotionStatus.ENDED;
	}
}
