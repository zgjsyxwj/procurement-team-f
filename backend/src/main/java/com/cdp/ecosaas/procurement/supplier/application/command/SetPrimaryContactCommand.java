package com.cdp.ecosaas.procurement.supplier.application.command;

/**
 * 设置主要联系人命令（Req 9.4）—— 自动取消原主要联系人的标记。
 *
 * @param supplierId 所属供应商 ID
 * @param contactId  目标联系人 ID
 */
public record SetPrimaryContactCommand(Long supplierId, Long contactId) {
}
