package com.cdp.ecosaas.procurement.auth.infrastructure.security;

import com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthCorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 配置 - JWT 无状态认证。
 *
 * <p>核心配置：
 * <ul>
 *   <li>禁用 Spring Security 内置 CSRF（由 JwtAuthenticationFilter 手动实现 Double Submit Cookie）</li>
 *   <li>禁用 session 管理（使用 JWT 无状态认证）</li>
 *   <li>配置公开路径（登录、SSO回调、忘记密码、重置密码）</li>
 *   <li>配置管理员专属路径（/api/admin/**）</li>
 *   <li>CORS 策略由 {@link AuthCorsProperties} 提供，按环境配置</li>
 *   <li>注册 JwtAuthenticationFilter 在 UsernamePasswordAuthenticationFilter 之前</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthCorsProperties corsProps;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthCorsProperties corsProps) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsProps = corsProps;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public paths - no authentication required
                        .requestMatchers(HttpMethod.POST, "/api/internal/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/internal/auth/sso/callback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/supplier/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                        // OpenAPI documentation
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                        // Actuator management endpoints
                        .requestMatchers(HttpMethod.GET, "/management/**").permitAll()
                        // Admin-only paths - require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)
                        // All other /api/** paths require authentication
                        .requestMatchers("/api/**").authenticated()
                        // Any other paths (non-API) are permitted
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProps.allowedOrigins());
        configuration.setAllowedMethods(corsProps.allowedMethods());
        configuration.setAllowedHeaders(corsProps.allowedHeaders());
        configuration.setAllowCredentials(corsProps.allowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
