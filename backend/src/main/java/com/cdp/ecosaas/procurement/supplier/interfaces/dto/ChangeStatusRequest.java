package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;

/**
 * 调整供应商状态请求（Req 7.7-7.11）。
 *
 * @param targetStatus 目标状态（ACTIVE/DISABLED）
 * @param remark       操作备注
 */
public record ChangeStatusRequest(SupplierStatus targetStatus, String remark) {
}
