package com.cdp.ecosaas.procurement.auth.infrastructure.security;

import com.cdp.ecosaas.procurement.auth.domain.port.SamlUserAttributes;
import com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthSamlProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SamlAuthProvider 单元测试
 */
class SamlAuthProviderTest {

    private SamlAuthProvider samlAuthProvider;

    @BeforeEach
    void setUp() {
        AuthSamlProperties props = new AuthSamlProperties(
                "ecosaas-procurement",
                "https://worklife.example.com/saml/metadata",
                "http://localhost:9000/api/internal/auth/sso/callback"
        );
        samlAuthProvider = new SamlAuthProvider(props);
    }

    @Nested
    @DisplayName("parseSamlResponse")
    class ParseSamlResponseTests {

        @Test
        @DisplayName("should parse valid SAML response and extract NameID and attributes")
        void shouldParseValidSamlResponse() {
            String samlXml = buildSamlResponse(
                    "user123@worklife.com",
                    "张三",
                    "zhangsan@example.com",
                    "13800138000"
            );
            String base64Response = Base64.getEncoder().encodeToString(
                    samlXml.getBytes(StandardCharsets.UTF_8));

            Map<String, String> result = samlAuthProvider.parseSamlResponse(base64Response);

            assertNotNull(result);
            assertEquals("user123@worklife.com", result.get("subjectId"));
            assertEquals("张三", result.get("displayName"));
            assertEquals("zhangsan@example.com", result.get("email"));
            assertEquals("13800138000", result.get("phone"));
        }

        @Test
        @DisplayName("should parse SAML response without phone attribute")
        void shouldParseSamlResponseWithoutPhone() {
            String samlXml = buildSamlResponse(
                    "user456@worklife.com",
                    "李四",
                    "lisi@example.com",
                    null
            );
            String base64Response = Base64.getEncoder().encodeToString(
                    samlXml.getBytes(StandardCharsets.UTF_8));

            Map<String, String> result = samlAuthProvider.parseSamlResponse(base64Response);

            assertNotNull(result);
            assertEquals("user456@worklife.com", result.get("subjectId"));
            assertEquals("李四", result.get("displayName"));
            assertEquals("lisi@example.com", result.get("email"));
            assertNull(result.get("phone"));
        }

        @Test
        @DisplayName("should throw exception for null SAML response")
        void shouldThrowForNullResponse() {
            assertThrows(IllegalArgumentException.class,
                    () -> samlAuthProvider.parseSamlResponse(null));
        }

        @Test
        @DisplayName("should throw exception for empty SAML response")
        void shouldThrowForEmptyResponse() {
            assertThrows(IllegalArgumentException.class,
                    () -> samlAuthProvider.parseSamlResponse(""));
        }

        @Test
        @DisplayName("should throw exception for invalid base64")
        void shouldThrowForInvalidBase64() {
            assertThrows(IllegalArgumentException.class,
                    () -> samlAuthProvider.parseSamlResponse("not-valid-base64!!!"));
        }

        @Test
        @DisplayName("should throw exception for invalid XML")
        void shouldThrowForInvalidXml() {
            String invalidXml = "<not-a-saml-response>";
            String base64 = Base64.getEncoder().encodeToString(
                    invalidXml.getBytes(StandardCharsets.UTF_8));

            assertThrows(IllegalArgumentException.class,
                    () -> samlAuthProvider.parseSamlResponse(base64));
        }

        @Test
        @DisplayName("should parse SAML response with cn attribute instead of displayName")
        void shouldParseSamlResponseWithCnAttribute() {
            String samlXml = buildSamlResponseWithCn(
                    "user789@worklife.com",
                    "王五",
                    "wangwu@example.com"
            );
            String base64Response = Base64.getEncoder().encodeToString(
                    samlXml.getBytes(StandardCharsets.UTF_8));

            Map<String, String> result = samlAuthProvider.parseSamlResponse(base64Response);

            assertNotNull(result);
            assertEquals("user789@worklife.com", result.get("subjectId"));
            assertEquals("王五", result.get("cn"));
            assertEquals("wangwu@example.com", result.get("email"));
        }
    }

    @Nested
    @DisplayName("extractUserAttributes")
    class ExtractUserAttributesTests {

        @Test
        @DisplayName("should extract all attributes including phone")
        void shouldExtractAllAttributes() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("subjectId", "user123@worklife.com");
            attributes.put("displayName", "张三");
            attributes.put("email", "zhangsan@example.com");
            attributes.put("phone", "13800138000");

            SamlUserAttributes result = samlAuthProvider.extractUserAttributes(attributes);

            assertEquals("user123@worklife.com", result.subjectId());
            assertEquals("张三", result.name());
            assertEquals("zhangsan@example.com", result.email());
            assertEquals("13800138000", result.phone());
        }

        @Test
        @DisplayName("should extract attributes with cn fallback for name")
        void shouldExtractWithCnFallback() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("subjectId", "user456@worklife.com");
            attributes.put("cn", "李四");
            attributes.put("mail", "lisi@example.com");

            SamlUserAttributes result = samlAuthProvider.extractUserAttributes(attributes);

            assertEquals("user456@worklife.com", result.subjectId());
            assertEquals("李四", result.name());
            assertEquals("lisi@example.com", result.email());
            assertNull(result.phone());
        }

        @Test
        @DisplayName("should prefer displayName over cn")
        void shouldPreferDisplayNameOverCn() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("subjectId", "user@worklife.com");
            attributes.put("displayName", "显示名");
            attributes.put("cn", "通用名");
            attributes.put("email", "user@example.com");

            SamlUserAttributes result = samlAuthProvider.extractUserAttributes(attributes);

            assertEquals("显示名", result.name());
        }

        @Test
        @DisplayName("should prefer email over mail")
        void shouldPreferEmailOverMail() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("subjectId", "user@worklife.com");
            attributes.put("displayName", "用户");
            attributes.put("email", "primary@example.com");
            attributes.put("mail", "secondary@example.com");

            SamlUserAttributes result = samlAuthProvider.extractUserAttributes(attributes);

            assertEquals("primary@example.com", result.email());
        }

        @Test
        @DisplayName("should use mobile as phone fallback")
        void shouldUseMobileAsFallback() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("subjectId", "user@worklife.com");
            attributes.put("displayName", "用户");
            attributes.put("email", "user@example.com");
            attributes.put("mobile", "13900139000");

            SamlUserAttributes result = samlAuthProvider.extractUserAttributes(attributes);

            assertEquals("13900139000", result.phone());
        }

        @Test
        @DisplayName("should allow null phone")
        void shouldAllowNullPhone() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("subjectId", "user@worklife.com");
            attributes.put("displayName", "用户");
            attributes.put("email", "user@example.com");

            SamlUserAttributes result = samlAuthProvider.extractUserAttributes(attributes);

            assertNull(result.phone());
        }

        @Test
        @DisplayName("should throw for null attributes")
        void shouldThrowForNullAttributes() {
            assertThrows(IllegalArgumentException.class,
                    () -> samlAuthProvider.extractUserAttributes(null));
        }

        @Test
        @DisplayName("should throw for empty attributes")
        void shouldThrowForEmptyAttributes() {
            assertThrows(IllegalArgumentException.class,
                    () -> samlAuthProvider.extractUserAttributes(new HashMap<>()));
        }

        @Test
        @DisplayName("should throw when subjectId is missing")
        void shouldThrowWhenSubjectIdMissing() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("displayName", "用户");
            attributes.put("email", "user@example.com");

            assertThrows(IllegalArgumentException.class,
                    () -> samlAuthProvider.extractUserAttributes(attributes));
        }

        @Test
        @DisplayName("should throw when name is missing")
        void shouldThrowWhenNameMissing() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("subjectId", "user@worklife.com");
            attributes.put("email", "user@example.com");

            assertThrows(IllegalArgumentException.class,
                    () -> samlAuthProvider.extractUserAttributes(attributes));
        }

        @Test
        @DisplayName("should throw when email is missing")
        void shouldThrowWhenEmailMissing() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("subjectId", "user@worklife.com");
            attributes.put("displayName", "用户");

            assertThrows(IllegalArgumentException.class,
                    () -> samlAuthProvider.extractUserAttributes(attributes));
        }
    }

    @Nested
    @DisplayName("SP Metadata Configuration")
    class SpMetadataTests {

        @Test
        @DisplayName("should expose SP entity ID")
        void shouldExposeEntityId() {
            assertEquals("ecosaas-procurement", samlAuthProvider.getEntityId());
        }

        @Test
        @DisplayName("should expose IdP metadata URL")
        void shouldExposeIdpMetadataUrl() {
            assertEquals("https://worklife.example.com/saml/metadata",
                    samlAuthProvider.getIdpMetadataUrl());
        }

        @Test
        @DisplayName("should expose ACS URL")
        void shouldExposeAcsUrl() {
            assertEquals("http://localhost:9000/api/internal/auth/sso/callback",
                    samlAuthProvider.getAcsUrl());
        }
    }

    // ========== Test Helper Methods ==========

    /**
     * Build a minimal SAML Response XML with standard attributes.
     */
    private String buildSamlResponse(String nameId, String displayName, String email, String phone) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<samlp:Response xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ");
        sb.append("xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">");
        sb.append("<saml:Assertion>");
        sb.append("<saml:Subject>");
        sb.append("<saml:NameID>").append(nameId).append("</saml:NameID>");
        sb.append("</saml:Subject>");
        sb.append("<saml:AttributeStatement>");
        sb.append("<saml:Attribute Name=\"urn:oid:2.16.840.1.113730.3.1.241\" FriendlyName=\"displayName\">");
        sb.append("<saml:AttributeValue>").append(displayName).append("</saml:AttributeValue>");
        sb.append("</saml:Attribute>");
        sb.append("<saml:Attribute Name=\"urn:oid:0.9.2342.19200300.100.1.3\" FriendlyName=\"email\">");
        sb.append("<saml:AttributeValue>").append(email).append("</saml:AttributeValue>");
        sb.append("</saml:Attribute>");
        if (phone != null) {
            sb.append("<saml:Attribute Name=\"urn:oid:2.5.4.20\" FriendlyName=\"phone\">");
            sb.append("<saml:AttributeValue>").append(phone).append("</saml:AttributeValue>");
            sb.append("</saml:Attribute>");
        }
        sb.append("</saml:AttributeStatement>");
        sb.append("</saml:Assertion>");
        sb.append("</samlp:Response>");
        return sb.toString();
    }

    /**
     * Build a SAML Response with cn attribute instead of displayName.
     */
    private String buildSamlResponseWithCn(String nameId, String cn, String email) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<samlp:Response xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ");
        sb.append("xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">");
        sb.append("<saml:Assertion>");
        sb.append("<saml:Subject>");
        sb.append("<saml:NameID>").append(nameId).append("</saml:NameID>");
        sb.append("</saml:Subject>");
        sb.append("<saml:AttributeStatement>");
        sb.append("<saml:Attribute Name=\"urn:oid:2.5.4.3\" FriendlyName=\"cn\">");
        sb.append("<saml:AttributeValue>").append(cn).append("</saml:AttributeValue>");
        sb.append("</saml:Attribute>");
        sb.append("<saml:Attribute Name=\"urn:oid:0.9.2342.19200300.100.1.3\" FriendlyName=\"email\">");
        sb.append("<saml:AttributeValue>").append(email).append("</saml:AttributeValue>");
        sb.append("</saml:Attribute>");
        sb.append("</saml:AttributeStatement>");
        sb.append("</saml:Assertion>");
        sb.append("</samlp:Response>");
        return sb.toString();
    }
}
