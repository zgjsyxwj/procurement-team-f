package com.cdp.ecosaas.procurement.auth.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 认证模块配置属性注册。
 * <p>
 * 集中启用所有 {@code @ConfigurationProperties}，避免在每个属性类上重复 {@code @ConfigurationPropertiesScan}。
 */
@Configuration
@EnableConfigurationProperties({
        AuthJwtProperties.class,
        AuthSamlProperties.class,
        AuthLockoutProperties.class,
        AuthCookieProperties.class,
        AuthPasswordProperties.class,
        AuthCorsProperties.class,
        AuthMailProperties.class
})
public class AuthPropertiesConfig {
}
