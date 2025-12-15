package org.nextme.promotion_service.monitoring.remediation;

import lombok.Builder;
import lombok.Getter;

/**
 * 자동 실행 가능한 해결 조치
 */
@Getter
@Builder
public class RemediationAction {

	/**
	 * 조치 유형
	 */
	private final ActionType type;

	/**
	 * 조치 설명
	 */
	private final String description;

	/**
	 * 조치에 필요한 파라미터 (JSON 형식)
	 */
	private final String parameters;

	/**
	 * 우선순위 (낮을수록 높음)
	 */
	private final int priority;

	public enum ActionType {
		/**
		 * Redis 캐시 초기화
		 */
		CLEAR_REDIS_CACHE("Redis 캐시 초기화"),

		/**
		 * 환경변수 변경
		 */
		UPDATE_ENV_VARIABLE("환경변수 변경"),

		/**
		 * DB 커넥션 풀 크기 조정
		 */
		ADJUST_DB_POOL("DB 커넥션 풀 크기 조정"),

		/**
		 * 서버 재시작
		 */
		RESTART_SERVER("서버 재시작"),

		/**
		 * 가비지 컬렉션 강제 실행
		 */
		FORCE_GC("가비지 컬렉션 강제 실행"),

		/**
		 * 스레드 풀 크기 조정
		 */
		ADJUST_THREAD_POOL("스레드 풀 크기 조정");

		private final String displayName;

		ActionType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
}
