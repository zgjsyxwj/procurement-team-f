package com.cdp.ecosaas.procurement.auth.domain.port;

/**
 * 邮件端口接口 - 定义认证相关邮件发送的领域契约。
 */
public interface EmailPort {

    /**
     * 发送初始密码邮件
     *
     * @param email    收件人邮箱
     * @param name     收件人姓名
     * @param password 初始密码
     */
    void sendInitialPassword(String email, String name, String password);

    /**
     * 发送密码重置链接邮件
     *
     * @param email     收件人邮箱
     * @param name      收件人姓名
     * @param resetLink 密码重置链接
     */
    void sendPasswordResetLink(String email, String name, String resetLink);

    /**
     * 发送密码变更通知邮件
     *
     * @param email 收件人邮箱
     * @param name  收件人姓名
     */
    void sendPasswordChangedNotification(String email, String name);
}
