package org.nextme.promotion_service.global.config;

import java.util.List;

import org.nextme.common.jwt.JwtTokenProvider;
import org.nextme.common.jwt.TokenBlacklistService;
import org.nextme.common.security.DirectJwtAuthenticationFilter;
import org.nextme.common.security.GatewayUserHeaderAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public GatewayUserHeaderAuthenticationFilter gatewayUserHeaderAuthenticationFilter() {
		return new GatewayUserHeaderAuthenticationFilter();
	}

	@Bean
	public DirectJwtAuthenticationFilter directJwtAuthenticationFilter(
		JwtTokenProvider jwtTokenProvider,
		TokenBlacklistService tokenBlacklistService
	) {
		return new DirectJwtAuthenticationFilter(
			jwtTokenProvider,
			tokenBlacklistService,
			List.of()
		);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		GatewayUserHeaderAuthenticationFilter gatewayUserHeaderAuthenticationFilter,
		DirectJwtAuthenticationFilter directJwtAuthenticationFilter
	) throws Exception {

		http
			.csrf(csrf -> csrf.disable())
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/health", "/public/**", "/v1/promotions/monitoring/remediation/execute").permitAll()
				.requestMatchers("/actuator/prometheus", "/actuator/health").permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(gatewayUserHeaderAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(directJwtAuthenticationFilter,
				GatewayUserHeaderAuthenticationFilter.class);

		return http.build();
	}
}

