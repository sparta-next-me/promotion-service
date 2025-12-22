package org.nextme.promotion_service.global.ratelimit;

import java.time.Duration;

import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;

/**
 * Rate Limiting 설정
 * IP 기반으로 요청 속도를 제한하여 서버 과부하를 방지
 */
@Slf4j
@Configuration
public class RateLimitConfig {

	// IP당 초당 최대 요청 수
	private static final int REQUESTS_PER_SECOND = 100;

	// IP당 분당 최대 요청 수
	private static final int REQUESTS_PER_MINUTE = 1000;

	/**
	 * IP별 Bucket 생성
	 * Token Bucket 알고리즘 사용:
	 * - 초당 100개 토큰 재충전
	 * - 최대 1000개 토큰 보유 가능
	 */
	public Bucket createBucket() {
		// 초당 제한: 100 req/s
		Bandwidth secondLimit = Bandwidth.classic(
			REQUESTS_PER_SECOND,
			Refill.intervally(REQUESTS_PER_SECOND, Duration.ofSeconds(1))
		);

		// 분당 제한: 1000 req/min (burst 허용)
		Bandwidth minuteLimit = Bandwidth.classic(
			REQUESTS_PER_MINUTE,
			Refill.intervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
		);

		return Bucket.builder()
			.addLimit(secondLimit)
			.addLimit(minuteLimit)
			.build();
	}

	/**
	 * Rate Limit 초과 시 대기 시간 계산
	 * @param bucket 해당 IP의 Bucket
	 * @return 대기 시간 (초)
	 */
	public long getWaitTime(Bucket bucket) {
		long nanos = bucket.tryConsumeAndReturnRemaining(1).getNanosToWaitForRefill();
		return Duration.ofNanos(nanos).getSeconds();
	}
}
