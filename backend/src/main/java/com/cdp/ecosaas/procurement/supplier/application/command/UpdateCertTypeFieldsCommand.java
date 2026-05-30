package com.cdp.ecosaas.procurement.supplier.application.command;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertTypeField;

import java.util.List;

/**
 * 维护证件类型差异化字段命令（Req 11.5）—— 整体替换该类型的字段定义。
 *
 * @param certTypeId 证件类型 ID
 * @param fields     差异化字段定义列表
 */
public record UpdateCertTypeFieldsCommand(Long certTypeId, List<CertTypeField> fields) {
}
