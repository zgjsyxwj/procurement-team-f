package com.cdp.ecosaas.procurement.auth.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 认证模块邮件配置。
 * <p>
 * 配置前缀：{@code auth.mail}。SMTP 主机/端口/凭据继承 Spring Boot 标准
 * {@code spring.mail.*}（由 {@code JavaMailSender} 自动装配），本类只承载与
 * 邮件内容/发件人相关的业务配置，避免与 {@code spring.mail.*} 重复。
 * <ul>
 *   <li>{@code from}：发件人邮箱地址（必填）</li>
 *   <li>{@code fromName}：发件人显示名（可与 from 邮箱组合为 RFC 5322 格式）</li>
 *   <li>{@code enabled}：邮件实际发送开关；关闭时 {@code EmailServiceAdapter} 仅记录日志（开发/测试常用）</li>
 *   <li>{@code connectionTimeout} / {@code readTimeout}：SMTP 网络超时</li>
 * </ul>
 */
@Validated
@ConfigurationProperties(prefix = "auth.mail")
public record AuthMailProperties(
        @NotBlank String from,
        @NotBlank String fromName,
        boolean enabled,
        @NotNull Duration connectionTimeout,
        @NotNull Duration readTimeout
) {
}
