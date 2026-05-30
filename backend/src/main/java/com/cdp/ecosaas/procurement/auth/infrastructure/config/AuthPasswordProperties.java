package com.cdp.ecosaas.procurement.auth.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 密码策略配置。
 * <p>
 * 配置前缀：{@code auth.password}。
 * <ul>
 *   <li>{@code minLength}：用户密码最小长度</li>
 *   <li>{@code generatedMinLength}/{@code generatedMaxLength}：系统生成随机密码长度区间</li>
 *   <li>{@code historyCount}：密码历史比对数量</li>
 *   <li>{@code resetTokenExpiry}：密码重置链接有效期（如 {@code 30m}）</li>
 *   <li>{@code resetBaseUrl}：密码重置链接基础 URL（不含 token 参数）</li>
 * </ul>
 */
@Validated
@ConfigurationProperties(prefix = "auth.password")
public record AuthPasswordProperties(
        @Min(6) int minLength,
        @Min(8) int generatedMinLength,
        @Min(8) int generatedMaxLength,
        @Min(1) int historyCount,
        @NotNull Duration resetTokenExpiry,
        @NotBlank String resetBaseUrl
) {
}
