package org.nextme.promotion_service.promotion.presentation;

import java.util.UUID;

import org.nextme.infrastructure.success.CustomResponse;
import org.nextme.promotion_service.promotion.application.PromotionParticipationService;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionJoinRequest;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionJoinResponse;
import org.springframework.http.ResponseEntity;
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

	private final PromotionParticipationService participationService;

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
