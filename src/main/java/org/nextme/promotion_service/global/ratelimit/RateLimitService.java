package org.nextme.promotion_service.global.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
// IP 별로 Bucket을 생성하고 관리
public class RateLimitService {

	private final RateLimitConfig rateLimitConfig;

	// IP별 Bucket 저장 (메모리 기반)
	private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

	/*
	IP에 대한 요청 허용 여부 확인
	@param ip 클라이언트 IP
	@return true: 허용, false: 거부
	 */
	public boolean allowRequest(String ip) {
		Bucket bucket = resolveBucket(ip);
		boolean allowed = bucket.tryConsume(1);

		if (!allowed) {
			log.warn("Rate limit exceeded for IP: {}", ip);
		}

		return allowed;
	}

	/*
	IP별 Bucket 조회 또는 생성
	@param ip 클라이언트 ID
	@return Bucket
	 */
	private Bucket resolveBucket(String ip) {
		return bucketCache.computeIfAbsent(ip, k -> {
			log.info("Creating new rate limit bucket for IP: {}", ip);
			return rateLimitConfig.createBucket();
		});
	}

	/*
	Rate Limit 초과 시 대기 시간 조회
	@param ip 클라이언트 IP
	@return 대기 시간 (초)
	 */
	public long getWaitTime(String ip) {
		Bucket bucket = resolveBucket(ip);
		return rateLimitConfig.getWaitTime(bucket);
	}

	/*
	특정 IP의 Bucket 초기화
	 */
	public void resetBucket(String ip) {
		bucketCache.remove(ip);
		log.info("Reset rate limit bucket for IP: {}", ip);
	}

	/*
	모든 Bucket 초기화
	 */
	public void resetAllBuckets() {
		bucketCache.clear();
		log.info("Reset all rate limit buckets");
	}
}
