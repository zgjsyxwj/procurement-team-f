package com.cdp.ecosaas.procurement.supplier.application.command;

import java.util.Map;

/**
 * 采购员直接编辑供应商基本信息命令（Req 49）—— 即时生效并记录一条已通过变更。
 *
 * @param supplierId    供应商 ID
 * @param changedFields 提交的基本信息字段（key -> 新值字符串），仅与当前值不同者计入变更
 */
public record UpdateSupplierInfoCommand(Long supplierId, Map<String, String> changedFields) {
}
