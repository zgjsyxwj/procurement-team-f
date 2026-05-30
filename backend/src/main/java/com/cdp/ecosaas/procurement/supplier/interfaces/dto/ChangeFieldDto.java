package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeField;

/**
 * 变更字段前后值 DTO（Req 5.2、50.2）。
 */
public record ChangeFieldDto(String fieldKey, String fieldLabel, String beforeValue, String afterValue) {

    public static ChangeFieldDto from(SupplierChangeField field) {
        return new ChangeFieldDto(field.getFieldKey(), field.getFieldLabel(),
                field.getBeforeValue(), field.getAfterValue());
    }
}
