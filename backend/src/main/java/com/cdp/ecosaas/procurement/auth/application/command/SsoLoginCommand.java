package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * SSO 登录命令
 *
 * @param samlResponse Base64 编码的 SAML Response
 */
public record SsoLoginCommand(String samlResponse) {
}
