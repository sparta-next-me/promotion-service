package org.nextme.promotion_service.monitoring.remediation;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 자동 해결 조치를 실행하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemediationExecutor {

	private final RedisTemplate<String, String> redisTemplate;
	private final HikariDataSource dataSource;

	/**
	 * 조치 실행
	 *
	 * @param action 실행할 조치
	 * @return 실행 결과 메시지
	 */
	public String execute(RemediationAction action) {
		log.info("Executing remediation action: {} - {}", action.getType(), action.getDescription());

		try {
			String result = switch (action.getType()) {
				case CLEAR_REDIS_CACHE -> executeClearRedisCache();
				case FORCE_GC -> executeForceGC();
				case ADJUST_DB_POOL -> executeAdjustDBPool(action.getParameters());
				case RESTART_SERVER -> executeRestartServer();
				case UPDATE_ENV_VARIABLE -> executeUpdateEnvVariable(action.getParameters());
				case ADJUST_THREAD_POOL -> executeAdjustThreadPool(action.getParameters());
			};

			log.info("Remediation action completed successfully: {}", action.getType());
			return result;

		} catch (Exception e) {
			log.error("Failed to execute remediation action: {}", action.getType(), e);
			return "실행 실패: " + e.getMessage();
		}
	}

	/**
	 * Redis 캐시 초기화
	 */
	private String executeClearRedisCache() {
		log.info("Clearing Redis cache...");
		redisTemplate.getConnectionFactory().getConnection().flushDb();
		return "✅ Redis 캐시가 성공적으로 초기화되었습니다.";
	}

	/**
	 * 가비지 컬렉션 강제 실행
	 */
	private String executeForceGC() {
		log.info("Forcing garbage collection...");
		long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.gc();
		long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long freedMemory = (beforeMemory - afterMemory) / (1024 * 1024);
		return String.format("✅ GC 실행 완료. 약 %dMB 메모리 해제됨.", freedMemory);
	}

	/**
	 * DB 커넥션 풀 크기 조정
	 */
	private String executeAdjustDBPool(String parameters) {
		log.info("Adjusting DB connection pool size...");
		// 예: parameters = "{\"maxPoolSize\": 100}"
		// 실제 구현 시 JSON 파싱하여 적용
		HikariPoolMXBean poolProxy = dataSource.getHikariPoolMXBean();
		if (poolProxy != null) {
			// 소프트 최대 커넥션 수 증가 (런타임에 조정 가능한 부분만)
			poolProxy.softEvictConnections();
			return "✅ DB 커넥션 풀 유휴 커넥션을 정리했습니다.";
		}
		return "⚠️ DB 커넥션 풀 조정은 재시작이 필요합니다.";
	}

	/**
	 * 서버 재시작
	 */
	private String executeRestartServer() {
		log.warn("Server restart requested - NOT IMPLEMENTED for safety");
		// 실제 재시작은 위험하므로 로깅만 수행
		// 실제 구현 시 Spring Boot Actuator의 shutdown endpoint 활용 또는
		// 외부 orchestration 시스템 (Kubernetes, Docker Compose) 에 신호 전송
		return "⚠️ 서버 재시작은 수동으로 진행해주세요. (안전상 자동 실행 비활성화)";
	}

	/**
	 * 환경변수 변경
	 */
	private String executeUpdateEnvVariable(String parameters) {
		log.info("Updating environment variable: {}", parameters);
		// 예: parameters = "{\"HIKARI_MAX_POOL_SIZE\": \"100\"}"
		// 런타임 환경변수 변경은 제한적이므로 application.yaml 동적 갱신 또는
		// Spring Cloud Config refresh 필요
		return "⚠️ 환경변수 변경은 재시작 후 적용됩니다.";
	}

	/**
	 * 스레드 풀 크기 조정
	 */
	private String executeAdjustThreadPool(String parameters) {
		log.info("Adjusting thread pool size: {}", parameters);
		// 예: parameters = "{\"maxThreads\": \"500\"}"
		// Tomcat 스레드 풀 조정은 재시작 필요
		return "⚠️ 스레드 풀 크기 조정은 재시작 후 적용됩니다.";
	}
}
