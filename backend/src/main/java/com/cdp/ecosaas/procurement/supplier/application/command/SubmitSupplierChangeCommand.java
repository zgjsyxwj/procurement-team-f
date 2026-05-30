package com.cdp.ecosaas.procurement.supplier.application.command;

import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeType;

import java.util.Map;

/**
 * 供应商提交基本信息变更命令（Req 3.3、4.2）。
 * <p>
 * 处理器按供应商状态分流：待完善信息（草稿）直接生效；合作中则落待审核变更。
 *
 * @param supplierId    供应商 ID
 * @param changeType    变更类型（本阶段 BASIC_INFO）
 * @param changedFields 提交的字段（key -> 新值字符串）
 */
public record SubmitSupplierChangeCommand(Long supplierId, ChangeType changeType, Map<String, String> changedFields) {
}
