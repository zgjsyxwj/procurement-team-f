package com.cdp.ecosaas.procurement.supplier.application.command;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCategory;

/**
 * 创建供应商命令（Req 6.1-6.3）。
 * <p>
 * 携带企业名称、分类与主要联系人（姓名/手机号/邮箱）；{@code sendInvitation} 区分
 * 「保存并发送邀请」（true→发邀请邮件、状态置「待进入」）与「仅保存」（false→状态「创建成功」）。
 */
public record CreateSupplierCommand(
        String name,
        SupplierCategory category,
        String contactName,
        String contactPhone,
        String contactEmail,
        boolean sendInvitation
) {
}
