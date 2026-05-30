package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import java.util.List;

/**
 * 维护证件类型差异化字段请求（整体替换，Req 11.5）。
 */
public record CertTypeFieldsRequest(List<CertTypeFieldDto> fields) {
}
