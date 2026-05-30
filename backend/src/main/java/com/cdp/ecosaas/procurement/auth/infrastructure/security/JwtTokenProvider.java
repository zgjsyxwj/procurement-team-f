package com.cdp.ecosaas.procurement.auth.infrastructure.security;

import com.cdp.ecosaas.procurement.auth.domain.port.TokenPort;
import com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 提供者 - 基于 JJWT 0.12.6 实现 Token 生成、验证与刷新。
 *
 * <p>Token 结构：
 * <ul>
 *   <li>sub: userId</li>
 *   <li>type: INTERNAL / SUPPLIER</li>
 *   <li>role: 用户角色</li>
 *   <li>name: 用户姓名</li>
 *   <li>iat: 签发时间</li>
 *   <li>exp: 过期时间（取决于配置 {@code auth.jwt.expiration}）</li>
 * </ul>
 *
 * <p>支持滑动过期：每次请求验证通过后可调用 {@link #refreshToken(String)} 生成新 Token。
 * 配置参见 {@link AuthJwtProperties}。
 */
@Component
public class JwtTokenProvider implements TokenPort {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtTokenProvider(AuthJwtProperties props) {
        this.secretKey = Keys.hmacShaKeyFor(props.secretKey().getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = props.expiration().toMillis();
    }

    @Override
    public String generateToken(Long userId, String userType, String role, String name) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", userType)
                .claim("role", role)
                .claim("name", name)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public Map<String, Object> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Map<String, Object> result = new HashMap<>();
            result.put("sub", claims.getSubject());
            result.put("type", claims.get("type", String.class));
            result.put("role", claims.get("role", String.class));
            result.put("name", claims.get("name", String.class));
            result.put("iat", claims.getIssuedAt());
            result.put("exp", claims.getExpiration());
            return result;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("Invalid JWT token format: {}", e.getMessage());
        } catch (SignatureException e) {
            log.debug("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT token is empty or null: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void invalidateToken(String token) {
        // 当前为无状态 JWT，登出通过清除 Cookie 实现。
        // 如需支持主动失效（如强制登出），可引入 Token 黑名单（Redis）机制；跟踪 issue: TBD。
    }

    /**
     * 刷新 Token - 使用原 Token 的 claims 生成新 Token，刷新过期时间（滑动过期）。
     *
     * @param token 当前有效的 JWT token
     * @return 新的 JWT token（过期时间重新计算），如果原 token 无效则返回 null
     */
    public String refreshToken(String token) {
        Map<String, Object> claims = validateToken(token);
        if (claims == null) {
            return null;
        }

        Long userId = Long.valueOf((String) claims.get("sub"));
        String userType = (String) claims.get("type");
        String role = (String) claims.get("role");
        String name = (String) claims.get("name");

        return generateToken(userId, userType, role, name);
    }
}
