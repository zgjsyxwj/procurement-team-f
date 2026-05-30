package com.cdp.ecosaas.procurement.auth.domain.port;

import java.util.Map;

/**
 * SAML 端口接口 - 定义 SAML 2.0 认证相关的领域契约。
 */
public interface SamlPort {

    /**
     * 解析 SAML Response
     *
     * @param samlResponse base64 encoded SAML response
     * @return parsed SAML attributes
     */
    Map<String, String> parseSamlResponse(String samlResponse);

    /**
     * 从 SAML Assertion 中提取用户属性
     *
     * @param attributes parsed SAML attributes
     * @return user attributes (subjectId, name, email, phone optional)
     */
    SamlUserAttributes extractUserAttributes(Map<String, String> attributes);
}
