package com.cdp.ecosaas.procurement.supplier.domain.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierInvitationLog;

/**
 * 供应商邀请日志仓储接口（领域层端口），由基础设施层实现。
 * <p>
 * 邀请日志为追加写（仅新增），记录邀请邮件发送时间与结果（Req 6.8、9.10）。
 */
public interface SupplierInvitationLogRepository {

    SupplierInvitationLog save(SupplierInvitationLog invitationLog);
}
