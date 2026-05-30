package com.cdp.ecosaas.procurement.auth.domain.port;

/**
 * SAML 用户属性值对象 - 从 SAML Assertion 中提取的用户信息。
 *
 * @param subjectId SAML Subject NameID
 * @param name      用户姓名
 * @param email     用户邮箱
 * @param phone     用户手机号（可选，可为 null）
 */
public record SamlUserAttributes(
        String subjectId,
        String name,
        String email,
        String phone
) {
}
