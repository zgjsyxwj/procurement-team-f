package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 供应商变更字段差异值对象（Req 5.2、50.2）。
 * <p>
 * 记录字段级前后值，用于审核对比展示与变更历史展示。银行/多值字段以序列化文本承载。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierChangeField {

    private String fieldKey;      // 字段标识
    private String fieldLabel;    // 字段显示名
    private String beforeValue;   // 变更前值
    private String afterValue;    // 变更后值
}
