package org.nextme.promotion_service.promotion.infrastructure.redis;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.nextme.promotion_service.promotion.domain.Promotion;
import org.nextme.promotion_service.promotion.domain.PromotionStatus;
import org.nextme.promotion_service.promotion.infrastructure.persistence.PromotionRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionCacheService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final PromotionRepository promotionRepository;

	private static final String CACHE_PREFIX = "promotion:cache:";
	private static final String ACTIVE_LIST_KEY = "promotion:active:list";
	private static final Duration CACHE_TTL = Duration.ofMinutes(5);

	// ACTIVE 상태 프로모션 ID 목록 조회 (캐시 사용)
	public List<UUID> getActivePromotionIds() {
		// 1. 캐시에서 조회
		List<Object> cacheIds = redisTemplate.opsForList().range(ACTIVE_LIST_KEY, 0, -1);

		if (cacheIds != null && !cacheIds.isEmpty()) {
			log.debug("캐시에서 활성 프로모션 조회 : {} 개", cacheIds.size());
			return cacheIds.stream()
				.map(obj -> UUID.fromString(obj.toString()))
				.collect(Collectors.toList());
		}

		// 2. 캐시 미스 - DB 조회
		log.debug("캐시 미스 - DB에서 활성 프로모션 조회");
		List<Promotion> activePromotions = promotionRepository.findByStatus(PromotionStatus.ACTIVE);

		if (activePromotions.isEmpty()) {
			return List.of();
		}

		// 3. 캐시에 저장
		List<String> promotionIds = activePromotions.stream()
			.map(p -> p.getId().toString())
			.collect(Collectors.toList());

		redisTemplate.opsForList().rightPushAll(ACTIVE_LIST_KEY, promotionIds);
		redisTemplate.expire(ACTIVE_LIST_KEY, CACHE_TTL);

		log.info("활성 프로모션 캐시 저장 : {} 개", promotionIds.size());

		return activePromotions.stream()
			.map(Promotion::getId)
			.collect(Collectors.toList());
	}

	// 프로모션 정보 조회 (캐시 사용)
	public Promotion getPromotion(UUID promotionId) {
		String cacheKey = CACHE_PREFIX + promotionId;

		// 1. 캐시에서 조회
		Promotion cached = (Promotion)redisTemplate.opsForValue().get(cacheKey);
		if (cached != null) {
			log.debug("캐시에서 프로모션 조회 : {}", promotionId);
			return cached;
		}

		// 2. 캐시 미스 - DB 조회
		log.debug("캐시 미스 - DB에서 프로모션 조회 : {}", promotionId);
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElse(null);

		if (promotion != null) {
			// 3. 캐시에 저장
			redisTemplate.opsForValue().set(cacheKey, promotion, CACHE_TTL);
			log.debug("프로모션 캐시 저장 : {}", promotionId);
		}

		return promotion;
	}

	// 캐시 무효화 (프로모션 상태 변경 시 호출)
	public void evictCache(UUID promotionId) {
		String cacheKey = CACHE_PREFIX + promotionId;
		redisTemplate.delete(cacheKey);
		redisTemplate.delete(ACTIVE_LIST_KEY);
		log.info("프로모션 캐시 삭제 : {}", promotionId);
	}

	// 모든 캐시 무효화
	public void evictAllCache() {
		redisTemplate.delete(ACTIVE_LIST_KEY);
		log.info("모든 활성 프로모션 캐시 삭제");
	}
}
