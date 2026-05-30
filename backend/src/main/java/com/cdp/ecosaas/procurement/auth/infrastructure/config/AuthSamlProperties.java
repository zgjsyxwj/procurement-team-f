package com.cdp.ecosaas.procurement.auth.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * SAML 2.0 SSO 配置。
 * <p>
 * 配置前缀：{@code auth.saml}。
 * <ul>
 *   <li>{@code entityId}：本服务作为 SP 的 Entity ID</li>
 *   <li>{@code idpMetadataUrl}：Worklife IdP 元数据 URL</li>
 *   <li>{@code acsUrl}：Assertion Consumer Service URL</li>
 * </ul>
 */
@Validated
@ConfigurationProperties(prefix = "auth.saml")
public record AuthSamlProperties(
        @NotBlank String entityId,
        @NotBlank String idpMetadataUrl,
        @NotBlank String acsUrl
) {
}
