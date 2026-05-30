package com.cdp.ecosaas.procurement.shared.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类（跨模块共享）。
 * <p>
 * 提供从 SecurityContext 中获取当前用户信息的便捷方法。
 * 所有模块的 Controller 可使用此工具类获取当前操作人信息。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户ID。
     *
     * @return 用户ID，未认证时返回 null
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        try {
            return Long.parseLong((String) auth.getPrincipal());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户角色。
     *
     * @return 角色字符串（如 "ADMIN"），未认证时返回 null
     */
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return null;
        }
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断当前用户是否已认证。
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() != null;
    }

    /**
     * 判断当前用户是否为管理员。
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }
}
