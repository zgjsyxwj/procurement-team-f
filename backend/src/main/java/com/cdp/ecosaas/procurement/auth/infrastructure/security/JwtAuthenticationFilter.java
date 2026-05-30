package com.cdp.ecosaas.procurement.auth.infrastructure.security;

import com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthCookieProperties;
import com.cdp.ecosaas.procurement.auth.shared.exception.AuthErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JWT 认证过滤器 - 从 Cookie 中提取 JWT Token 进行认证。
 *
 * <p>核心职责：
 * <ul>
 *   <li>从配置的 JWT Cookie 中提取 Token（非 Authorization Header）</li>
 *   <li>验证 Token 有效性并设置 SecurityContext</li>
 *   <li>实现滑动过期：验证通过后刷新 Cookie 过期时间</li>
 *   <li>CSRF Token 校验：写操作验证请求头中的 CSRF token 与 Cookie 中的值一致</li>
 * </ul>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/internal/auth/login",
            "/api/internal/auth/sso/callback",
            "/api/supplier/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieWriter cookieWriter;
    private final AuthCookieProperties cookieProps;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   AuthCookieWriter cookieWriter,
                                   AuthCookieProperties cookieProps) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieWriter = cookieWriter;
        this.cookieProps = cookieProps;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractCookie(request, cookieProps.jwtName());

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Map<String, Object> claims = jwtTokenProvider.validateToken(token);

        if (claims == null) {
            cookieWriter.clearJwtCookie(response);
            filterChain.doFilter(request, response);
            return;
        }

        if (WRITE_METHODS.contains(request.getMethod()) && !validateCsrfToken(request)) {
            log.debug("CSRF token validation failed for {} {}", request.getMethod(), request.getRequestURI());
            writeCsrfFailureResponse(response);
            return;
        }

        String userId = (String) claims.get("sub");
        String role = (String) claims.get("role");

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sliding expiry
        String refreshedToken = jwtTokenProvider.refreshToken(token);
        if (refreshedToken != null) {
            cookieWriter.setJwtCookie(response, refreshedToken);
        }

        filterChain.doFilter(request, response);
    }

    private String extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean validateCsrfToken(HttpServletRequest request) {
        String headerToken = request.getHeader(cookieProps.csrfHeaderName());
        if (headerToken == null || headerToken.isBlank()) {
            return false;
        }

        String cookieToken = extractCookie(request, cookieProps.csrfCookieName());
        if (cookieToken == null || cookieToken.isBlank()) {
            return false;
        }

        return headerToken.equals(cookieToken);
    }

    private void writeCsrfFailureResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        AuthErrorCode err = AuthErrorCode.CSRF_TOKEN_MISMATCH;
        String body = "{\"code\":\"" + err.getCode() + "\","
                + "\"message\":\"" + err.getMessage() + "\","
                + "\"detail\":\"CSRF验证失败\"}";
        response.getWriter().write(body);
    }
}
