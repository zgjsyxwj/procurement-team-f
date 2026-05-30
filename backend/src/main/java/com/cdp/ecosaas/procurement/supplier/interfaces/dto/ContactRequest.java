package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

/**
 * 新增/编辑联系人请求（Req 9.1）。
 */
public record ContactRequest(String name, String phone, String email, boolean primary,
                             String position, String department) {
}
