package com.cdp.ecosaas.procurement.auth.domain.port;

/**
 * 密码编码器端口接口 - 定义密码哈希与验证的领域契约。
 * <p>
 * 将 Spring Security 的 BCryptPasswordEncoder 从领域层解耦。
 */
public interface PasswordEncoderPort {

    /**
     * 对明文密码进行哈希
     *
     * @param rawPassword 明文密码
     * @return 哈希后的密码
     */
    String encode(String rawPassword);

    /**
     * 验证明文密码与哈希是否匹配
     *
     * @param rawPassword    明文密码
     * @param encodedPassword 哈希后的密码
     * @return true 表示匹配
     */
    boolean matches(String rawPassword, String encodedPassword);
}
