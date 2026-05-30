package com.cdp.ecosaas.procurement.supplier.application.command;

import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeType;

/**
 * 供应商撤回待审核变更命令（Req 3.7）。
 *
 * @param supplierId 供应商 ID
 * @param changeType 变更类型（撤回该类型下的待审核变更）
 */
public record WithdrawChangeCommand(Long supplierId, ChangeType changeType) {
}
