package com.cdp.ecosaas.procurement.supplier.application.command;

/**
 * 删除联系人命令（Req 9.5、9.6）—— 不可删除唯一的主要联系人。
 *
 * @param supplierId 所属供应商 ID
 * @param contactId  待删除联系人 ID
 */
public record DeleteContactCommand(Long supplierId, Long contactId) {
}
