package com.cdp.ecosaas.procurement.supplier.infrastructure.external;

import com.cdp.ecosaas.procurement.supplier.domain.port.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 邮件适配器 —— {@link EmailPort} 的占位实现（任务 6.3）。
 * <p>
 * 按决策：邮件本轮不接入真实 SMTP，所有发信方法「直接成功返回」（仅记录日志、不实际发送），
 * 以便上层业务流程（邀请、变更通知、证件通知/提醒）正常推进而不被发信阻塞、不抛异常。
 * 待后续接入真实邮件服务时，可参照模块 01 {@code auth.infrastructure.external.EmailServiceAdapter}
 * 复用 {@code spring.mail.*} / {@code auth.mail} 配置补齐。
 */
@Slf4j
@Component("supplierEmailServiceAdapter")
public class EmailServiceAdapter implements EmailPort {

    @Override
    public void sendSupplierInvitation(String email, String contactName, String supplierName,
                                       String loginPhone, String initialPassword) {
        log.info("[邮件占位·直接成功] 供应商邀请 -> {}（供应商: {}, 登录手机号: {}）", email, supplierName, loginPhone);
    }

    @Override
    public void sendChangePendingNotification(String email, String supplierName, String changeType) {
        log.info("[邮件占位·直接成功] 变更待审核通知 -> {}（供应商: {}, 变更类型: {}）", email, supplierName, changeType);
    }

    @Override
    public void sendChangeReviewResult(String email, String supplierName, boolean approved, String comment) {
        log.info("[邮件占位·直接成功] 变更审核结果 -> {}（供应商: {}, 通过: {}）", email, supplierName, approved);
    }

    @Override
    public void sendCertificateRejected(String email, String supplierName, String certTypeName, String reason) {
        log.info("[邮件占位·直接成功] 证件驳回通知 -> {}（供应商: {}, 证件类型: {}）", email, supplierName, certTypeName);
    }

    @Override
    public void sendCertificateExpiryReminder(String email, String supplierName, String certTypeName,
                                              LocalDate validTo, long remainingDays) {
        log.info("[邮件占位·直接成功] 证件到期提醒 -> {}（供应商: {}, 证件类型: {}, 截止: {}, 剩余: {}天）",
                email, supplierName, certTypeName, validTo, remainingDays);
    }
}
