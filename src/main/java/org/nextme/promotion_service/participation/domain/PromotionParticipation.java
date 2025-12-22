package org.nextme.promotion_service.participation.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.nextme.common.jpa.BaseEntity;
import org.nextme.promotion_service.promotion.domain.Promotion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_promotion_participation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionParticipation extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	// 참여한 프로모션
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "promotion_id", nullable = false)
	private Promotion promotion;

	// 참여한 사용자 ID
	@Column(nullable = false)
	private UUID userId;

	// 참여 시각
	@Column(nullable = false)
	private LocalDateTime participatedAt;

	// 대기열 순번 (당첨 시 몇 번째로 당첨되었는지)
	@Column
	private Long queuePosition;

	// 참여 상태
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ParticipationStatus status;

	// 참여 시점의 사용자 IP 주소
	@Column(length = 45)
	private String ipAddress;

	// 참여 시점의 User-Agent
	@Column(length = 500)
	private String userAgent;

	// 당첨 여부 확인
	public boolean isWinner() {
		return this.status == ParticipationStatus.WON;
	}

	// 당첨자 생성
	public static PromotionParticipation createWinner(
		Promotion promotion,
		UUID userId,
		String ipAddress,
		String userAgent,
		Long position
	) {
		PromotionParticipation participation = new PromotionParticipation();
		participation.promotion = promotion;
		participation.userId = userId;
		participation.ipAddress = ipAddress;
		participation.userAgent = userAgent;
		participation.participatedAt = LocalDateTime.now();
		participation.status = ParticipationStatus.WON;
		participation.queuePosition = position;
		return participation;
	}

	// 탈락자 생성
	public static PromotionParticipation createLoser(
		Promotion promotion,
		UUID userId,
		String ipAddress,
		String userAgent
	) {
		PromotionParticipation participation = new PromotionParticipation();
		participation.promotion = promotion;
		participation.userId = userId;
		participation.ipAddress = ipAddress;
		participation.userAgent = userAgent;
		participation.participatedAt = LocalDateTime.now();
		participation.status = ParticipationStatus.LOST;
		return participation;
	}
}
