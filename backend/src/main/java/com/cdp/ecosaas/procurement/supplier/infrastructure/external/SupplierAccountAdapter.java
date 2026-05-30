package com.cdp.ecosaas.procurement.supplier.infrastructure.external;

import com.cdp.ecosaas.procurement.auth.domain.model.SupplierUser;
import com.cdp.ecosaas.procurement.auth.domain.repository.SupplierUserRepository;
import com.cdp.ecosaas.procurement.supplier.domain.port.SupplierAccountPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 供应商账号适配器 —— {@link SupplierAccountPort} 实现（任务 6.2a / 9.x）。
 * <p>
 * 账号创建/停用/启用与模块 01 的真实联动按决策延后至任务 17.2（清晰失败的桩，抛
 * {@link UnsupportedOperationException}，避免静默以假账号继续）。{@code findSupplierIdByUserId}
 * 为只读解析，直接复用模块 01 {@link SupplierUserRepository}（与 {@code BuyerSupplierRelationAdapter}
 * 同模式，仅读、不改模块 01 代码），供应商门户端据此确定本企业数据范围。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierAccountAdapter implements SupplierAccountPort {

    private static final String NOT_WIRED =
            "供应商账号开通尚未接入模块01，按决策延后至任务17.2跨模块联调";

    private final SupplierUserRepository supplierUserRepository;

    @Override
    public String createAccount(Long supplierId, String contactName, String phone, String email) {
        log.warn("[未接入] 创建供应商账号 supplierId={}, phone={} —— {}", supplierId, phone, NOT_WIRED);
        throw new UnsupportedOperationException(NOT_WIRED);
    }

    @Override
    public Optional<Long> findSupplierIdByUserId(Long userId) {
        return supplierUserRepository.findById(userId).map(SupplierUser::getSupplierId);
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
