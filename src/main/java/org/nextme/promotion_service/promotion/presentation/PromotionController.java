package org.nextme.promotion_service.promotion.presentation;

import java.util.UUID;

import org.nextme.common.security.UserPrincipal;
import org.nextme.infrastructure.success.CustomResponse;
import org.nextme.promotion_service.promotion.application.PromotionParticipationService;
import org.nextme.promotion_service.promotion.application.PromotionService;
import org.nextme.promotion_service.promotion.domain.PromotionStatus;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionCreateRequest;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionJoinResponse;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionResponse;
import org.nextme.promotion_service.promotion.presentation.dto.PromotionStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion", description = "프로모션 관리 API")
public class PromotionController {

	private final PromotionService promotionService;
	private final PromotionParticipationService participationService;

	/*
	프로모션 생성 API
	@param request 프로모션 생성 요청
	@return 생성된 프로모션 정보
	 */
	@PreAuthorize("hasRole('MANAGER')")
	@Operation(summary = "프로모션 생성", description = "새로운 프로모션을 생성합니다.")
	@PostMapping
	public ResponseEntity<CustomResponse<PromotionResponse>> createPromotion(
		@Valid @RequestBody PromotionCreateRequest request
	) {
		PromotionResponse response = promotionService.createPromotion(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(CustomResponse.onSuccess(response));
	}

	/*
	프로모션 목록 조회 API
	@param status 프로모션 상태 (선택)
	@param pageable 페이징 정보
	@return 프로모션 목록
	 */
	@Operation(summary = "프로모션 목록 조회", description = "전체 또는 상태별 프로모션 목록을 페이징하여 조회합니다.")
	@GetMapping
	public ResponseEntity<CustomResponse<Page<PromotionResponse>>> getPromotions(
		@Parameter(description = "프로모션 상태 (SCHEDULED, ACTIVE, ENDED)")
		@RequestParam(required = false) PromotionStatus status,
		@Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
		@RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지 크기", example = "20")
		@RequestParam(defaultValue = "20") int size
	) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<PromotionResponse> response = promotionService.getPromotions(status, pageable);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}


	/*
	프로모션 조회 API
	@param promotionId 프로모션 ID
	@return 프로모션 정보
	 */
	@Operation(summary = "프로모션 조회", description = "프로모션 ID로 상세 정보를 조회합니다.")
	@GetMapping("/{promotionId}")
	public ResponseEntity<CustomResponse<PromotionResponse>> getPromotion(
		@Parameter(description = "프로모션 ID", required = true)
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
	@PreAuthorize("hasRole('MANAGER')")
	@Operation(summary = "프로모션 시작", description = "프로모션을 ACTIVE 상태로 변경합니다.")
	@PatchMapping("/{promotionId}/start")
	public ResponseEntity<CustomResponse<PromotionResponse>> startPromotion(
		@Parameter(description = "프로모션 ID", required = true)
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
	@PreAuthorize("hasRole('MANAGER')")
	@Operation(summary = "프로모션 종료", description = "프로모션을 ENDED 상태로 변경합니다.")
	@PatchMapping("/{promotionId}/end")
	public ResponseEntity<CustomResponse<PromotionResponse>> endPromotion(
		@Parameter(description = "프로모션 ID", required = true)
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
	@PreAuthorize("hasRole('MANAGER')")
	@Operation(summary = "프로모션 참여 현황 조회", description = "대기열 크기, 참여자 수, 당첨자 수 등의 현황을 조회합니다.")
	@GetMapping("/{promotionId}/status")
	public ResponseEntity<CustomResponse<PromotionStatusResponse>> getPromotionStatus(
		@Parameter(description = "프로모션 ID", required = true)
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
		@Parameter(description = "프로모션 ID", required = true)
		@PathVariable UUID promotionId,
		HttpServletRequest httpRequest,
		@AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		PromotionJoinResponse response = participationService.joinPromotion(
			promotionId,
			UUID.fromString(userPrincipal.userId()),
			httpRequest);
		return ResponseEntity.ok(CustomResponse.onSuccess(response));
	}
}
