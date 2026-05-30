package com.cdp.ecosaas.procurement.auth.domain.port;

import java.util.Map;

/**
 * Token 端口接口 - 定义 Token 生成、验证和失效的领域契约。
 */
public interface TokenPort {

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param userType 用户类型 (INTERNAL/SUPPLIER)
     * @param role     用户角色
     * @param name     用户姓名
     * @return JWT token string
     */
    String generateToken(Long userId, String userType, String role, String name);

    /**
     * 验证并解析 Token
     *
     * @param token JWT token string
     * @return token claims as a Map, or null if invalid
     */
    Map<String, Object> validateToken(String token);

    /**
     * 使 Token 失效（用于登出等场景）
     *
     * @param token JWT token string
     */
    void invalidateToken(String token);
}
