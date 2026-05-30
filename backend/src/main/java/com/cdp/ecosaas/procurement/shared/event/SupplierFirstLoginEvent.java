package com.cdp.ecosaas.procurement.shared.event;

/**
 * 供应商首次登录事件（跨模块契约）。
 * <p>
 * 由模块 01（认证）在供应商账号首次登录成功时发布；模块 02（供应商管理）的
 * {@code SupplierFirstLoginListener} 监听后将供应商状态由「创建成功/待进入」流转为「待完善信息」（Req 7.3）。
 * 置于 shared 以解耦两模块（双方仅依赖本契约，互不直接引用对方应用层）。
 *
 * @param supplierId 首次登录账号所属的供应商企业 ID
 */
public record SupplierFirstLoginEvent(Long supplierId) {
}
