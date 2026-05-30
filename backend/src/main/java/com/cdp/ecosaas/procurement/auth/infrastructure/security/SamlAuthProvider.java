package com.cdp.ecosaas.procurement.auth.infrastructure.security;

import com.cdp.ecosaas.procurement.auth.domain.port.SamlPort;
import com.cdp.ecosaas.procurement.auth.domain.port.SamlUserAttributes;
import com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthSamlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * SAML 认证提供者 - 实现 SAML 2.0 Service Provider 功能。
 *
 * <p>负责解析 IdP 返回的 SAML Response，提取用户属性（NameID、姓名、邮箱、手机号）。
 * JIT Provisioning 逻辑由应用层 AuthCommandHandler 处理，本组件仅负责 SAML 协议解析。
 *
 * <p>SP 元数据配置由 {@link AuthSamlProperties} 提供。
 *
 * <p>生产环境需要增加以下安全加固（跟踪 issue: TBD）：
 * <ul>
 *   <li>验证 SAML Response 的 XML 签名（使用 IdP 证书）</li>
 *   <li>验证 Assertion 的 Conditions（NotBefore / NotOnOrAfter）</li>
 *   <li>验证 Destination 与 ACS URL 一致</li>
 *   <li>验证 InResponseTo 防止重放攻击</li>
 *   <li>使用 OpenSAML 或 Spring Security SAML 库替代手动 XML 解析</li>
 * </ul>
 */
@Component
public class SamlAuthProvider implements SamlPort {

    private static final Logger log = LoggerFactory.getLogger(SamlAuthProvider.class);

    // Standard SAML attribute name URIs（SAML 协议固定值，§12.3 例外允许硬编码）
    private static final String ATTR_DISPLAY_NAME = "urn:oid:2.16.840.1.113730.3.1.241";
    private static final String ATTR_CN = "urn:oid:2.5.4.3";
    private static final String ATTR_EMAIL = "urn:oid:0.9.2342.19200300.100.1.3";
    private static final String ATTR_MAIL = "urn:oid:1.2.840.113549.1.9.1";
    private static final String ATTR_PHONE = "urn:oid:2.5.4.20";
    private static final String ATTR_MOBILE = "urn:oid:0.9.2342.19200300.100.1.41";

    // SAML XML namespace（SAML 2.0 协议常量）
    private static final String SAML_NS = "urn:oasis:names:tc:SAML:2.0:assertion";

    private final AuthSamlProperties props;

    public SamlAuthProvider(AuthSamlProperties props) {
        this.props = props;
        log.info("SAML SP initialized - entityId: {}, acsUrl: {}, idpMetadataUrl: {}",
                props.entityId(), props.acsUrl(), props.idpMetadataUrl());
    }

    @Override
    public Map<String, String> parseSamlResponse(String samlResponse) {
        if (samlResponse == null || samlResponse.isBlank()) {
            throw new IllegalArgumentException("SAML Response cannot be null or empty");
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(samlResponse);
            String xml = new String(decodedBytes, StandardCharsets.UTF_8);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // XXE protection
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            Map<String, String> attributes = new HashMap<>();

            String nameId = extractNameId(document);
            if (nameId != null) {
                attributes.put("subjectId", nameId);
            }

            extractSamlAttributes(document, attributes);

            log.debug("Parsed SAML Response - subjectId: {}, attributes: {}",
                    nameId, attributes.keySet());

            return attributes;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse SAML Response: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid SAML Response: " + e.getMessage(), e);
        }
    }

    @Override
    public SamlUserAttributes extractUserAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("SAML attributes cannot be null or empty");
        }

        String subjectId = attributes.get("subjectId");
        if (subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("SAML NameID (subjectId) is required");
        }

        String name = getFirstNonBlank(attributes, "displayName", "cn");
        if (name == null) {
            throw new IllegalArgumentException("SAML attribute for name (displayName or cn) is required");
        }

        String email = getFirstNonBlank(attributes, "email", "mail");
        if (email == null) {
            throw new IllegalArgumentException("SAML attribute for email (email or mail) is required");
        }

        String phone = getFirstNonBlank(attributes, "phone", "mobile");

        log.debug("Extracted SAML user attributes - subjectId: {}, name: {}, email: {}, phone: {}",
                subjectId, name, email, phone != null ? "***" : "null");

        return new SamlUserAttributes(subjectId, name, email, phone);
    }

    public String getEntityId() {
        return props.entityId();
    }

    public String getIdpMetadataUrl() {
        return props.idpMetadataUrl();
    }

    public String getAcsUrl() {
        return props.acsUrl();
    }

    // ========== Private Helper Methods ==========

    private String extractNameId(Document document) {
        NodeList nameIdNodes = document.getElementsByTagNameNS(SAML_NS, "NameID");
        if (nameIdNodes.getLength() > 0) {
            String nameId = nameIdNodes.item(0).getTextContent();
            return nameId != null ? nameId.trim() : null;
        }
        return null;
    }

    private void extractSamlAttributes(Document document, Map<String, String> attributes) {
        NodeList attrNodes = document.getElementsByTagNameNS(SAML_NS, "Attribute");

        for (int i = 0; i < attrNodes.getLength(); i++) {
            Element attrElement = (Element) attrNodes.item(i);
            String attrName = attrElement.getAttribute("Name");
            String friendlyName = attrElement.getAttribute("FriendlyName");

            NodeList valueNodes = attrElement.getElementsByTagNameNS(SAML_NS, "AttributeValue");
            if (valueNodes.getLength() == 0) {
                continue;
            }
            String attrValue = valueNodes.item(0).getTextContent();
            if (attrValue == null || attrValue.isBlank()) {
                continue;
            }
            attrValue = attrValue.trim();

            mapAttribute(attributes, attrName, friendlyName, attrValue);
        }
    }

    private void mapAttribute(Map<String, String> attributes, String attrName, String friendlyName, String value) {
        if (ATTR_DISPLAY_NAME.equals(attrName) || "displayName".equalsIgnoreCase(friendlyName)) {
            attributes.put("displayName", value);
        } else if (ATTR_CN.equals(attrName) || "cn".equalsIgnoreCase(friendlyName)
                || "commonName".equalsIgnoreCase(friendlyName)) {
            attributes.put("cn", value);
        } else if (ATTR_EMAIL.equals(attrName) || ATTR_MAIL.equals(attrName)
                || "email".equalsIgnoreCase(friendlyName) || "mail".equalsIgnoreCase(friendlyName)) {
            attributes.put("email", value);
        } else if (ATTR_PHONE.equals(attrName) || "phone".equalsIgnoreCase(friendlyName)
                || "telephoneNumber".equalsIgnoreCase(friendlyName)) {
            attributes.put("phone", value);
        } else if (ATTR_MOBILE.equals(attrName) || "mobile".equalsIgnoreCase(friendlyName)) {
            attributes.put("mobile", value);
        } else {
            log.trace("Unrecognized SAML attribute: name={}, friendlyName={}, value={}",
                    attrName, friendlyName, value);
        }
    }

    private String getFirstNonBlank(Map<String, String> attributes, String... keys) {
        for (String key : keys) {
            String value = attributes.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
