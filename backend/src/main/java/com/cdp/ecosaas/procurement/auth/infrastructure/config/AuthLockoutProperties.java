package com.cdp.ecosaas.procurement.auth.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 账号锁定策略配置。
 * <p>
 * 配置前缀：{@code auth.lockout}。
 * <ul>
 *   <li>{@code maxFailedAttempts}：触发锁定的最大连续失败次数</li>
 *   <li>{@code lockDuration}：锁定持续时间（支持 ISO-8601 Duration 字面量，如 {@code 30m}、{@code PT30M}）</li>
 * </ul>
 */
@Validated
@ConfigurationProperties(prefix = "auth.lockout")
public record AuthLockoutProperties(
        @Min(1) int maxFailedAttempts,
        @NotNull Duration lockDuration
) {
}
