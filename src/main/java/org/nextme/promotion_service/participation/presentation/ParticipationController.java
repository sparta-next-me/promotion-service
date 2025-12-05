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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Participation", description = "프로모션 참여 결과 조회 API")
public class ParticipationController {

	private final ParticipationQueryService participationQueryService;

	/*
	특정 사용자의 참여 결과 조회 API
	@param promotionId 프로모션 ID
	@param userId 사용자 ID
	@return 참여 결과
	 */
	@Operation(summary = "특정 사용자의 참여 결과 조회", description = "프로모션에 참여한 사용자의 당첨/탈락 결과를 조회합니다.")
	@GetMapping("/{promotionId}/participations/{userId}")
	public ResponseEntity<CustomResponse<ParticipationResultResponse>> getParticipationResult(
		@Parameter(description = "프로모션 ID", required = true)
		@PathVariable UUID promotionId,
		@Parameter(description = "사용자 ID", required = true)
		@PathVariable UUID userId
	) {
		ParticipationResultResponse response = participationQueryService.getParticipationResult(promotionId, userId);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}

	/*
	당첨자 목록 조회 API
	@param promotionId 프로모션 ID
	@return 당첨자 목록
	 */
	@Operation(summary = "당첨자 목록 조회", description = "프로모션의 당첨자 목록을 당첨 순번 순으로 조회합니다.")
	@GetMapping("/{promotionId}/winners")
	public ResponseEntity<CustomResponse<List<WinnerListResponse>>> getWinners(
		@Parameter(description = "프로모션 ID", required = true)
		@PathVariable UUID promotionId
	) {
		List<WinnerListResponse> response = participationQueryService.getWinners(promotionId);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}
}
