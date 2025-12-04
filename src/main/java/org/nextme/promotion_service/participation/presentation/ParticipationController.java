package org.nextme.promotion_service.participation.presentation;

import java.util.List;
import java.util.UUID;

import org.nextme.infrastructure.success.CustomResponse;
import org.nextme.promotion_service.participation.application.ParticipationQueryService;
import org.nextme.promotion_service.participation.presentation.dto.ParticipationResultResponse;
import org.nextme.promotion_service.participation.presentation.dto.WinnerListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/promotions")
@RequiredArgsConstructor
public class ParticipationController {

	private final ParticipationQueryService participationQueryService;

	/*
	특정 사용자의 참여 결과 조회 API
	@param promotionId 프로모션 ID
	@param userId 사용자 ID
	@return 참여 결과
	 */
	@GetMapping("/{promotionId}/participations/{userId}")
	public ResponseEntity<CustomResponse<ParticipationResultResponse>> getParticipationResult(
		@PathVariable UUID promotionId,
		@PathVariable Long userId
	) {
		ParticipationResultResponse response = participationQueryService.getParticipationResult(promotionId, userId);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}

	/*
	당첨자 목록 조회 API
	@param promotionId 프로모션 ID
	@return 당첨자 목록
	 */
	@GetMapping("/{promotionId}/winners")
	public ResponseEntity<CustomResponse<List<WinnerListResponse>>> getWinners(
		@PathVariable UUID promotionId
	) {
		List<WinnerListResponse> response = participationQueryService.getWinners(promotionId);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}
}
