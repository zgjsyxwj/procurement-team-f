package com.cdp.ecosaas.procurement.supplier.application.command;

/**
 * 新增/编辑证件类型命令（Req 11.1-11.3）。
 * <p>
 * {@code id} 为 null 表示新增（名称唯一校验），非 null 表示编辑（仅在改名时校验唯一）。
 *
 * @param id     证件类型 ID（null=新增）
 * @param name   证件类型名称（必填、唯一）
 * @param remark 备注（选填）
 */
public record SaveCertTypeCommand(Long id, String name, String remark) {
}
