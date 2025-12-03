package org.nextme.promotion_service.promotion.infrastructure.redis;

import java.util.UUID;

/*
Redis Key 생성 유틸리티
프로모션 관련 Redis Key를 일관되게 생성
 */
public class RedisKeyGenerator {

	private static final String PREFIX = "promotion:";

	/*
	대기열 Key 생성
	promotion:{promotionId}:queue
	 */
	public static String queueKey(UUID promotionId) {
		return PREFIX + promotionId + ":queue";
	}

	/*
	참여 기록 Set Key 생성 (중복 방지용)
	promotion:{promotionId}:joined
	 */
	public static String joinedKey(UUID promotionId) {
		return PREFIX + promotionId + ":joined";
	}

	/*
	당첨자 카운트 Key 생성
	promotion:{promotionId}:stock
	 */
	public static String stockKey(UUID promotionId) {
		return PREFIX + promotionId + ":stock";
	}
}
