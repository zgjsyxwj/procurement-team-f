package com.cdp.ecosaas.procurement.auth.infrastructure.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * CORS 配置。
 * <p>
 * 配置前缀：{@code auth.cors}。允许源在不同环境差异较大，必须配置外部化。
 */
@Validated
@ConfigurationProperties(prefix = "auth.cors")
public record AuthCorsProperties(
        @NotEmpty List<String> allowedOrigins,
        @NotEmpty List<String> allowedMethods,
        @NotEmpty List<String> allowedHeaders,
        boolean allowCredentials
) {
}
