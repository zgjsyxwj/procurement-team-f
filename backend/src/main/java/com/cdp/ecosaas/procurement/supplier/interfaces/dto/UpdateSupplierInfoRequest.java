package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import java.util.Map;

/**
 * 采购员直接编辑供应商基本信息请求（Req 49）。
 * <p>
 * 字段以 key→新值字符串提交，key 取自基本信息字段注册表（{@code SupplierBasicInfoFields}）；仅与当前值不同者计入变更。
 */
public record UpdateSupplierInfoRequest(Map<String, String> changedFields) {
}
