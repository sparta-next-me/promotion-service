package org.nextme.promotion_service.promotion.infrastructure.redis;

import java.util.UUID;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionQueueService {

	private final RedisTemplate<String, Object> redisTemplate;

	/*
	대기열 등록
	@param promotionId 프로모션 ID
	@param data 저장할 데이터 (예 : "userId:ip:timestamp")
	 */
	public void enqueue(UUID promotionId, String data) {
		String queueKey = RedisKeyGenerator.queueKey(promotionId);
		redisTemplate.opsForList().rightPush(queueKey, data);
	}

	/*
	대기열에서 데이터 꺼내기 (워커용)
	@param promotionId 프로모션 ID
	@return 대기열 맨 앞 데이터, 없으면 null
	 */
	public String dequeue(UUID promotionId) {
		String queueKey = RedisKeyGenerator.queueKey(promotionId);
		Object value = redisTemplate.opsForList().leftPop(queueKey);
		return value != null ? value.toString() : null;
	}

	/*
	중복 참여 체크 및 기록
	@param promotionId 프로모션 ID
	@param userId 사용자 ID
	@return true : 신규 참여, false : 중복 참여
	 */
	public boolean addToJoinedSet(UUID promotionId, UUID userId) {
		String joinedKey = RedisKeyGenerator.joinedKey(promotionId);
		Long result = redisTemplate.opsForSet().add(joinedKey, userId.toString());
		return result != null && result == 1;
	}

	/*
	참여 기록에서 제거 (롤백용)
	@param promotionId 프로모션 ID
	@param userId 사용자 ID
	 */
	public void removeFromJoinedSet(UUID promotionId, UUID userId) {
		String joinedKey = RedisKeyGenerator.joinedKey(promotionId);
		redisTemplate.opsForSet().remove(joinedKey, userId.toString());
	}

	/*
	대기열 크기 조회
	@param promotionId 프로모션 ID
	@param userId 사용자 ID
	 */
	public Long getQueueSize(UUID promotionId) {
		String queueKey = RedisKeyGenerator.queueKey(promotionId);
		Long size = redisTemplate.opsForList().size(queueKey);
		return size != null ? size : 0L;
	}

	/*
	당첨자 수 증가 (원자적)
	@param promotionId 프로모션 ID
	@return 증가 후 당첨자 수
	 */
	public Long incrementWinnerCount(UUID promotionId) {
		String stockKey = RedisKeyGenerator.stockKey(promotionId);
		return redisTemplate.opsForValue().increment(stockKey);
	}

	/*
	현재 당첨자 수 조회
	@param promotionId 프로모션 ID
	@return 현재 당첨자 수
	 */
	public Long getWinnerCount(UUID promotionId) {
		String stockKey = RedisKeyGenerator.stockKey(promotionId);
		Object value = redisTemplate.opsForValue().get(stockKey);
		if (value == null) {
			return 0L;
		}
		return Long.parseLong(value.toString());
	}

	/*
	총 참여자 수 조회
	@param promotionId 프로모션 ID
	@return 총 참여자 수 (joined set 크기)
	 */
	public Long getParticipantCount(UUID promotionId) {
		String joinedKey = RedisKeyGenerator.joinedKey(promotionId);
		Long count = redisTemplate.opsForSet().size(joinedKey);
		return count != null ? count : 0L;
	}

	/*
	참여 처리 파이프라인 (중복 체크 + 큐 크기 + 큐 삽입)
	@param promotionId 프로모션 ID
	@param userId 사용자 ID
	@param queueData 큐에 저장할 데이터
	@return ParticipationResult (신규 참여 여부, 큐 크기)
	 */
	@SuppressWarnings("unchecked")
	public ParticipationResult addParticipantWithPipeline(UUID promotionId, UUID userId, String queueData) {
		String joinedKey = RedisKeyGenerator.joinedKey(promotionId);
		String queueKey = RedisKeyGenerator.queueKey(promotionId);

		// Serializer 직접 생성
		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();

		// 파이프라인 실행
		var results = redisTemplate.executePipelined((RedisCallback<Object>)connection -> {
			connection.setCommands().sAdd(
				stringSerializer.serialize(joinedKey),
				valueSerializer.serialize(userId.toString())
			);
			connection.listCommands().lLen(stringSerializer.serialize(queueKey));
			connection.listCommands().rPush(
				stringSerializer.serialize(queueKey),
				valueSerializer.serialize(queueData)
			);
			return null;
		});

		// 결과 추출
		Long addResult = (Long) results.get(0);  // SADD 결과 (1=신규, 0=중복)
		Long queueSize = (Long) results.get(1);  // LLEN 결과

		return new ParticipationResult(
			addResult != null && addResult == 1L,
			queueSize != null ? queueSize : 0L
		);
	}

	// 파이프라인 결과
	public static class ParticipationResult {
		private final boolean isNewParticipant;
		private final Long queueSize;

		public ParticipationResult(boolean isNewParticipant, Long queueSize) {
			this.isNewParticipant = isNewParticipant;
			this.queueSize = queueSize;
		}

		public boolean isNewParticipant() {
			return isNewParticipant;
		}

		public Long getQueueSize() {
			return queueSize;
		}
	}
}
