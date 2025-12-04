package org.nextme.promotion_service.promotion.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionWinnerEvent {

	private UUID promotionId;		// 프로모션 ID
	private String promotionName;	// 프로모션 이름
	private Long userId;			// 사용자 ID
	private Integer pointAmount;	// 지급할 포인트 금액
	private Long queuePosition;		// 당첨 순번
	private LocalDateTime wonAt;	// 당첨 시각

	public static PromotionWinnerEvent of(
		UUID promotionId,
		String promotionName,
		Long userId,
		Integer pointAmount,
		Long queuePosition
	) {
		return new PromotionWinnerEvent(
			promotionId,
			promotionName,
			userId,
			pointAmount,
			queuePosition,
			LocalDateTime.now()
		);
	}
}
