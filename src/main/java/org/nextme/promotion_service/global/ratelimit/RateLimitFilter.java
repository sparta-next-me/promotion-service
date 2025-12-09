package org.nextme.promotion_service.global.ratelimit;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

	private final RateLimitService rateLimitService;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		// 프로모션 참여 API에만 Rate Limiting 적용
		String requestURI = request.getRequestURI();
		if (!requestURI.matches(".*/v1/promotions/.*/join")) {
			filterChain.doFilter(request, response);
			return;
		}

		// 클라이언트 IP 추출
		String clientIp = getClientIp(request);

		// Rate Limit 확인
		if (!rateLimitService.allowRequest(clientIp)) {
			// Rate Limit 초과
			long waitTime = rateLimitService.getWaitTime(clientIp);

			log.warn("Rate limit exceed - IP: {}, waitTime: {}s", clientIp, waitTime);

			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(String.format(
				"{\"success\":false,\"message\":\"요청이 너무 많습니다. %d초 후 다시 시도해주세요.\",\"data\":null}", waitTime
			));

			return;
		}

		// Rate Limit 통과
		filterChain.doFilter(request, response);
	}

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
