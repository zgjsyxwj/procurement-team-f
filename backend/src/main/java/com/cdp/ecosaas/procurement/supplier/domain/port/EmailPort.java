package com.cdp.ecosaas.procurement.supplier.domain.port;

import java.time.LocalDate;

/**
 * 邮件端口（出站）—— 定义供应商模块相关邮件发送的领域契约。
 * <p>
 * 实现见 {@code EmailServiceAdapter}（任务 6.3），复用模块 01 的邮件配置发送。
 */
public interface EmailPort {

    /**
     * 发送供应商入驻邀请邮件（登录手机号 + 初始密码，Req 6.3）。
     */
    void sendSupplierInvitation(String email, String contactName, String supplierName,
                                String loginPhone, String initialPassword);

    /**
     * 通知关联采购员有新的信息变更待审核（Req 3.4）。
     */
    void sendChangePendingNotification(String email, String supplierName, String changeType);

    /**
     * 通知供应商信息变更审核结果（通过/驳回，Req 5.4）。
     */
    void sendChangeReviewResult(String email, String supplierName, boolean approved, String comment);

    /**
     * 通知供应商证件审核被驳回（Req 10.8）。
     */
    void sendCertificateRejected(String email, String supplierName, String certTypeName, String reason);

    /**
     * 发送证件到期提醒（含企业名称、证件类型、截止日期、剩余天数，Req 12.2、12.4）。
     */
    void sendCertificateExpiryReminder(String email, String supplierName, String certTypeName,
                                       LocalDate validTo, long remainingDays);
}
