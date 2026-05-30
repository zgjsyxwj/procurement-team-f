package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

/**
 * 审核驳回请求（驳回原因，Req 5.4、10.8）。
 */
public record ReviewRequest(String reason) {
}
