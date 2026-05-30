package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateType;

import java.util.List;

/**
 * 证件类型响应（含差异化字段，Req 11.1、11.6）。
 */
public record CertTypeResponse(Long id, String name, String status, String remark, List<CertTypeFieldDto> fields) {

    public static CertTypeResponse from(CertificateType type) {
        List<CertTypeFieldDto> fields = type.getFields() == null ? List.of()
                : type.getFields().stream().map(CertTypeFieldDto::from).toList();
        return new CertTypeResponse(type.getId(), type.getName(),
                type.getStatus() == null ? null : type.getStatus().name(), type.getRemark(), fields);
    }
}
