package com.cdp.ecosaas.procurement.auth.infrastructure.security;

import com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthCookieProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * 集中处理 JWT 与 CSRF Cookie 的写入和清除。
 * <p>
 * Cookie 名称、Path、MaxAge、Secure、SameSite 等属性全部来自
 * {@link AuthCookieProperties} 配置（参见 backend_spec §12.3）。
 */
@Component
public class AuthCookieWriter {

    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private static final String ATTR_HTTP_ONLY = "HttpOnly";
    private static final String ATTR_SECURE = "Secure";

    private final AuthCookieProperties props;

    public AuthCookieWriter(AuthCookieProperties props) {
        this.props = props;
    }

    public AuthCookieProperties properties() {
        return props;
    }

    /**
     * 写入 JWT Cookie（HttpOnly + 可选 Secure + SameSite）。
     */
    public void setJwtCookie(HttpServletResponse response, String token) {
        long maxAgeSeconds = props.maxAge().getSeconds();
        StringBuilder sb = new StringBuilder()
                .append(props.jwtName()).append('=').append(token)
                .append("; Path=").append(props.jwtPath())
                .append("; Max-Age=").append(maxAgeSeconds)
                .append("; ").append(ATTR_HTTP_ONLY)
                .append("; SameSite=").append(props.sameSite());
        if (props.secure()) {
            sb.append("; ").append(ATTR_SECURE);
        }
        response.addHeader(SET_COOKIE_HEADER, sb.toString());
    }

    /**
     * 清除 JWT Cookie（Max-Age=0）。
     */
    public void clearJwtCookie(HttpServletResponse response) {
        StringBuilder sb = new StringBuilder()
                .append(props.jwtName()).append('=')
                .append("; Path=").append(props.jwtPath())
                .append("; Max-Age=0")
                .append("; ").append(ATTR_HTTP_ONLY)
                .append("; SameSite=").append(props.sameSite());
        if (props.secure()) {
            sb.append("; ").append(ATTR_SECURE);
        }
        response.addHeader(SET_COOKIE_HEADER, sb.toString());
    }

    /**
     * 写入 CSRF Cookie（非 HttpOnly，便于前端读取）。
     */
    public void setCsrfCookie(HttpServletResponse response, String csrfToken) {
        long maxAgeSeconds = props.maxAge().getSeconds();
        StringBuilder sb = new StringBuilder()
                .append(props.csrfCookieName()).append('=').append(csrfToken)
                .append("; Path=").append(props.csrfPath())
                .append("; Max-Age=").append(maxAgeSeconds)
                .append("; SameSite=").append(props.sameSite());
        if (props.secure()) {
            sb.append("; ").append(ATTR_SECURE);
        }
        response.addHeader(SET_COOKIE_HEADER, sb.toString());
    }

    /**
     * 清除 CSRF Cookie。
     */
    public void clearCsrfCookie(HttpServletResponse response) {
        StringBuilder sb = new StringBuilder()
                .append(props.csrfCookieName()).append('=')
                .append("; Path=").append(props.csrfPath())
                .append("; Max-Age=0")
                .append("; SameSite=").append(props.sameSite());
        if (props.secure()) {
            sb.append("; ").append(ATTR_SECURE);
        }
        response.addHeader(SET_COOKIE_HEADER, sb.toString());
    }
}
