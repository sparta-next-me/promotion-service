package org.nextme.promotion_service.promotion.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.nextme.promotion_service.global.exception.PromotionErrorCode;
import org.nextme.promotion_service.promotion.domain.Promotion;
import org.nextme.promotion_service.promotion.infrastructure.persistence.PromotionRepository;
import org.nextme.promotion_service.promotion.infrastructure.redis.PromotionQueueService;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionJoinResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionParticipationService {

	private final PromotionRepository promotionRepository;
	private final PromotionQueueService queueService;

	// 대기열 최대 크기 = 선착순 인원 * 5
	private static final int QUEUE_SIZE_MULTIPLIER = 5;

	/*
	프로모션 참여 처리
	@param promotionId 프로모션 ID
	@param userId 사용자 ID
	@param ipAddress 사용자 IP 주소
	@return 참여 결과
	 */
	@Transactional(readOnly = true)
	public PromotionJoinResponse joinPromotion(UUID promotionId, UUID userId, HttpServletRequest httpRequest) {

		// 클라이언트 IP 추출
		String ipAddress = getClientIp(httpRequest);
		String userAgent = httpRequest.getHeader("User-Agent");

		log.info("프로모션 참여 요청 - promotionId: {}, userId: {}, ip: {}", promotionId, userId, ipAddress);

		// 1. 프로모션 조회
		Promotion promotion = promotionRepository.findById(promotionId)
			.orElseThrow(PromotionErrorCode.PROMOTION_NOT_FOUND::toException);

		// 2. 프로모션 참여 가능 여부 확인
		if (!promotion.canParticipate()) {
			throw PromotionErrorCode.PROMOTION_NOT_AVAILABLE.toException();
		}

		// 3. 중복 체크 + 참여 기록
		boolean isNewParticipant = queueService.addToJoinedSet(promotionId, userId);
		if (!isNewParticipant) {
			throw PromotionErrorCode.PROMOTION_ALREADY_JOINED.toException();
		}

		// 4. 대기열 크기 확인
		Long queueSize = queueService.getQueueSize(promotionId);
		int maxQueueSize = promotion.getTotalStock() * QUEUE_SIZE_MULTIPLIER;

		if (queueSize >= maxQueueSize) {
			// 대기열 초과 시 롤백
			queueService.removeFromJoinedSet(promotionId, userId);
			throw PromotionErrorCode.PROMOTION_QUEUE_FULL.toException();
		}

		// 5. 대기열 등록
		String queueData = String.format("%s:%s:%s:%s", userId, ipAddress, userAgent, LocalDateTime.now());
		queueService.enqueue(promotionId, queueData);

		// 6. 성공 응답
		Long position = queueSize + 1;
		log.info("프로모션 참여 성공 - promotionId: {}, userId: {}, position: {}", promotionId, userId, position);
		return PromotionJoinResponse.success("대기열에 진입했습니다.", position);
	}

	// 클라이언트 IP 주소 추출
	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");

		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		// X-Forwarded-For에 여러 IP가 있을 경우 첫 번째 IP 추출
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0].trim();
		}

		return ip;
	}
}
