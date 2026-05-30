package com.cdp.ecosaas.procurement.auth.shared.constants;

/**
 * 认证模块共享常量。
 * <p>
 * 仅放置真正的领域常量（永不随环境变化的契约值）。
 * 业务策略阈值（锁定次数、Token 有效期等）已外部化到
 * {@link com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthLockoutProperties}、
 * {@link com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthPasswordProperties}、
 * {@link com.cdp.ecosaas.procurement.auth.infrastructure.config.AuthCookieProperties}
 * 等配置类中（参见 backend_spec §12.3）。
 */
public final class AuthConstants {

    /** 内部用户类型标识（domain 契约的一部分） */
    public static final String USER_TYPE_INTERNAL = "INTERNAL";

    /** 供应商用户类型标识（domain 契约的一部分） */
    public static final String USER_TYPE_SUPPLIER = "SUPPLIER";

    /** 审计日志结果：成功 */
    public static final String RESULT_SUCCESS = "SUCCESS";

    /** 审计日志结果：失败 */
    public static final String RESULT_FAILURE = "FAILURE";

    private AuthConstants() {
        // 工具类禁止实例化
    }
}
