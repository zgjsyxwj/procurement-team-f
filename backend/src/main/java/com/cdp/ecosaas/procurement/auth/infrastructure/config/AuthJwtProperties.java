package com.cdp.ecosaas.procurement.auth.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * JWT 配置。
 * <p>
 * 配置前缀：{@code auth.jwt}。
 * <ul>
 *   <li>{@code secretKey}：HMAC-SHA 签名密钥（生产应至少 32 字节，从密钥管理服务下发）</li>
 *   <li>{@code expiration}：Token 有效期（支持 ISO-8601 Duration，如 {@code 30m}、{@code PT30M}）</li>
 * </ul>
 */
@Validated
@ConfigurationProperties(prefix = "auth.jwt")
public record AuthJwtProperties(
        @NotBlank @Size(min = 32) String secretKey,
        @NotNull Duration expiration
) {
}
