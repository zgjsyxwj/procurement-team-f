package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 证件类型差异化字段值对象（Req 11.5）。
 * <p>
 * 供应商选择证件类型后据此动态渲染上传表单。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertTypeField {

    private String fieldKey;    // 字段标识
    private String fieldLabel;  // 字段显示名
    private String fieldType;   // TEXT/NUMBER/DATE/SELECT...
    private boolean required;
    private int sortOrder;
}
