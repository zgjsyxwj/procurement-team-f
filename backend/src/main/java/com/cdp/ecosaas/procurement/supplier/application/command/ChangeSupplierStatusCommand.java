package com.cdp.ecosaas.procurement.supplier.application.command;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;

/**
 * 采购员调整供应商状态命令（Req 7.7-7.11）—— 直接调整为「合作中」或「已停用」，无需额外审批。
 *
 * @param supplierId   供应商 ID
 * @param targetStatus 目标状态，仅支持 {@code ACTIVE}（合作中）/{@code DISABLED}（已停用）
 * @param remark       操作备注（Req 7.10）
 */
public record ChangeSupplierStatusCommand(Long supplierId, SupplierStatus targetStatus, String remark) {
}
