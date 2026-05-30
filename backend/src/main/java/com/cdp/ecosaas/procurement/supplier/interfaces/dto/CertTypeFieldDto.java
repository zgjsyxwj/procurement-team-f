package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertTypeField;

/**
 * 证件类型差异化字段 DTO（Req 11.5、11.6）。
 */
public record CertTypeFieldDto(String fieldKey, String fieldLabel, String fieldType,
                               boolean required, int sortOrder) {

    public static CertTypeFieldDto from(CertTypeField field) {
        return new CertTypeFieldDto(field.getFieldKey(), field.getFieldLabel(),
                field.getFieldType(), field.isRequired(), field.getSortOrder());
    }

    public CertTypeField toDomain() {
        return CertTypeField.builder()
                .fieldKey(fieldKey).fieldLabel(fieldLabel).fieldType(fieldType)
                .required(required).sortOrder(sortOrder).build();
    }
}
