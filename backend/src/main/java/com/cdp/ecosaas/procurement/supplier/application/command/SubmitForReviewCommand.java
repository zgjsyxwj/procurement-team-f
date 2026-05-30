package com.cdp.ecosaas.procurement.supplier.application.command;

/**
 * 供应商提交准入审核命令（Req 4.4）—— 待完善信息 → 待审核信息。
 *
 * @param supplierId 供应商 ID
 */
public record SubmitForReviewCommand(Long supplierId) {
}
