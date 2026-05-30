package com.cdp.ecosaas.procurement.supplier.application.command;

/**
 * 启用/停用证件类型命令（Req 11.1、11.4）—— 停用保留历史证件数据，仅新上传时不再展示。
 *
 * @param certTypeId 证件类型 ID
 * @param active     true=启用；false=停用
 */
public record ChangeCertTypeStatusCommand(Long certTypeId, boolean active) {
}
