package com.example.token_learn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security設定クラス
 *
 * <p>ROPCフロー（/auth/token）のみを提供するシンプルなセキュリティ設定。</p>
 * <ul>
 *   <li>ステートレス（セッションなし）</li>
 *   <li>CSRFなし（REST APIのため不要）</li>
 *   <li>/auth/token、/auth/refresh、/actuator/health、Swagger UIのみ許可</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                authz -> authz
                    .requestMatchers(
                        "/auth/token",
                        "/auth/refresh",
                        "/actuator/health",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                    )
                    .permitAll()
                    .anyRequest()
                    .denyAll()
            );
        return http.build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Token Learn API")
                    .description("ROPCフローでアクセストークンを取得する学習用API")
                    .version("1.0.0")
            );
    }
}
