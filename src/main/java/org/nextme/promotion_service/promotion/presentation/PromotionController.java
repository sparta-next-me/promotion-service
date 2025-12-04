package org.nextme.promotion_service.promotion.presentation;

import java.util.UUID;

import org.nextme.infrastructure.success.CustomResponse;
import org.nextme.promotion_service.promotion.application.PromotionParticipationService;
import org.nextme.promotion_service.promotion.application.PromotionService;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionCreateRequest;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionJoinRequest;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionJoinResponse;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionResponse;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionStatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

	private final PromotionService promotionService;
	private final PromotionParticipationService participationService;

	/*
	프로모션 생성 API
	@param request 프로모션 생성 요청
	@return 생성된 프로모션 정보
	 */
	@PostMapping
	public ResponseEntity<CustomResponse<PromotionResponse>> createPromotion(
		@Valid @RequestBody PromotionCreateRequest request
	) {
		PromotionResponse response = promotionService.createPromotion(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(CustomResponse.onSuccess(response));
	}

	/*
	프로모션 조회 API
	@param promotionId 프로모션 ID
	@return 프로모션 정보
	 */
	@GetMapping("/{promotionId}")
	public ResponseEntity<CustomResponse<PromotionResponse>> getPromotion(
		@PathVariable UUID promotionId
	) {
		PromotionResponse response = promotionService.getPromotion(promotionId);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}

	/*
	프로모션 시작 API
	@param promotionId 프로모션 ID
	@return 시작된 프로모션 정보
	 */
	@PatchMapping("/{promotionId}/start")
	public ResponseEntity<CustomResponse<PromotionResponse>> startPromotion(
		@PathVariable UUID promotionId
	) {
		PromotionResponse response = promotionService.startPromotion(promotionId);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}

	/*
	프로모션 종료 API
	@param promotionId 프로모션 ID
	@return 종료된 프로모션 정보
	 */
	@PatchMapping("/{promotionId}/end")
	public ResponseEntity<CustomResponse<PromotionResponse>> endPromotion(
		@PathVariable UUID promotionId
	) {
		PromotionResponse response = promotionService.endPromotion(promotionId);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}

	/*
	프로모션 참여 현황 조회 API
	@param promotionId 프로모션 ID
	@return 참여 현황 정보
	 */
	@GetMapping("/{promotionId}/status")
	public ResponseEntity<CustomResponse<PromotionStatusResponse>> getPromotionStatus(
		@PathVariable UUID promotionId
	) {
		PromotionStatusResponse response = promotionService.getPromotionStatus(promotionId);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}

	/*
	프로모션 참여 API
	@param promotionId 프로모션 ID
	@param request 참여 요청
	@param httpRequest HTTP 요청 (IP 추출용)
	@return 참여 결과
	 */
	@PostMapping("/{promotionId}/join")
	public ResponseEntity<CustomResponse<PromotionJoinResponse>> joinPromotion(
		@PathVariable UUID promotionId,
		@Valid @RequestBody PromotionJoinRequest request,
		HttpServletRequest httpRequest
	) {
		PromotionJoinResponse response = participationService.joinPromotion(promotionId, request.userId(), httpRequest);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}
}
