package com.cdp.ecosaas.procurement.auth.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Cookie 与 CSRF 配置。
 * <p>
 * 配置前缀：{@code auth.cookie}。
 * <ul>
 *   <li>{@code jwtName} / {@code jwtPath}：JWT Cookie 名称与作用域</li>
 *   <li>{@code csrfCookieName} / {@code csrfHeaderName} / {@code csrfPath}：CSRF Double Submit Cookie 配置</li>
 *   <li>{@code maxAge}：Cookie 存活时间（与 JWT 过期对齐）</li>
 *   <li>{@code secure} / {@code sameSite}：Cookie 安全属性</li>
 * </ul>
 */
@Validated
@ConfigurationProperties(prefix = "auth.cookie")
public record AuthCookieProperties(
        @NotBlank String jwtName,
        @NotBlank String jwtPath,
        @NotBlank String csrfCookieName,
        @NotBlank String csrfHeaderName,
        @NotBlank String csrfPath,
        @NotNull Duration maxAge,
        boolean secure,
        @NotBlank String sameSite
) {
}
