package com.cdp.ecosaas.procurement.supplier.application.command;

/**
 * 新增/编辑联系人命令（Req 9.1、9.6、9.8）。
 * <p>
 * {@code contactId} 为 null 表示新增，非 null 表示编辑；编辑即时生效，无需审批。
 *
 * @param supplierId 所属供应商 ID
 * @param contactId  联系人 ID（null=新增）
 * @param name       姓名（必填）
 * @param phone      手机号（必填）
 * @param email      邮箱（必填）
 * @param primary    是否设为主要联系人（设主时自动取消原主要联系人）
 * @param position   职务（选填）
 * @param department 部门（选填）
 */
public record SaveContactCommand(Long supplierId, Long contactId, String name, String phone,
                                 String email, boolean primary, String position, String department) {
}
