package com.cdp.ecosaas.procurement.supplier.infrastructure.external;

import com.cdp.ecosaas.procurement.supplier.domain.port.SupplierAccountPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 供应商账号适配器 —— {@link SupplierAccountPort} 的占位桩（任务 6.2a）。
 * <p>
 * 模块 01 当前没有「开通供应商账号」的对外应用服务，账号创建/停用/启用与模块 01 的真实联动
 * 按决策延后至任务 17.2（跨模块集成联调）。在此之前提供一个清晰失败的桩：调用即记录日志并抛
 * {@link UnsupportedOperationException}，避免静默地以假账号继续业务流程。
 */
@Slf4j
@Component
public class SupplierAccountAdapter implements SupplierAccountPort {

    private static final String NOT_WIRED =
            "供应商账号开通尚未接入模块01，按决策延后至任务17.2跨模块联调";

    @Override
    public String createAccount(Long supplierId, String contactName, String phone, String email) {
        log.warn("[未接入] 创建供应商账号 supplierId={}, phone={} —— {}", supplierId, phone, NOT_WIRED);
        throw new UnsupportedOperationException(NOT_WIRED);
    }

    @Override
    public void disableAccount(Long supplierId) {
        log.warn("[未接入] 停用供应商账号 supplierId={} —— {}", supplierId, NOT_WIRED);
        throw new UnsupportedOperationException(NOT_WIRED);
    }

    @Override
    public void enableAccount(Long supplierId) {
        log.warn("[未接入] 启用供应商账号 supplierId={} —— {}", supplierId, NOT_WIRED);
        throw new UnsupportedOperationException(NOT_WIRED);
    }
}
