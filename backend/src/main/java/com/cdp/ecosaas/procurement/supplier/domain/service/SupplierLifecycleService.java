package com.cdp.ecosaas.procurement.supplier.domain.service;

import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.port.SupplierAccountPort;

/**
 * 供应商生命周期领域服务（状态机，Req 7）。
 * <p>
 * 供应商状态流转的统一入口：转换规则由 {@link Supplier} 聚合根校验（非法流转抛
 * {@code InvalidSupplierStatusException}）；停用/启用时联动模块 01 登录账号
 * （通过 {@link SupplierAccountPort}）。无状态、不依赖 Spring，账号端口以方法参数注入
 * （与 {@code PasswordDomainService} 风格一致）。
 */
public class SupplierLifecycleService {

    /** 发送/重发邀请（Req 6.7）。 */
    public void invite(Supplier supplier) {
        supplier.invite();
    }

    /** 供应商首次登录流转（Req 7.3）。 */
    public void markFirstLogin(Supplier supplier) {
        supplier.onFirstLogin();
    }

    /** 提交准入审核（Req 4.4）。 */
    public void submitForReview(Supplier supplier) {
        supplier.submitForReview();
    }

    /** 审核通过（Req 5.3）。 */
    public void approve(Supplier supplier) {
        supplier.approve();
    }

    /** 审核驳回（Req 5.4）。 */
    public void reject(Supplier supplier) {
        supplier.reject();
    }

    /** 手动设为合作中（Req 7.9）。 */
    public void activate(Supplier supplier) {
        supplier.activate();
    }

    /**
     * 停用供应商并同步停用其模块 01 登录账号（Req 7.7、7.11）。
     * <p>
     * 先校验并执行状态流转（非法流转抛异常，此时不会调用账号端口），再停用账号。
     */
    public void disable(Supplier supplier, SupplierAccountPort accountPort) {
        supplier.disable();
        accountPort.disableAccount(supplier.getId());
    }

    /**
     * 重新启用供应商并同步启用其模块 01 登录账号（Req 7.8）。
     */
    public void enable(Supplier supplier, SupplierAccountPort accountPort) {
        supplier.enable();
        accountPort.enableAccount(supplier.getId());
    }
}
