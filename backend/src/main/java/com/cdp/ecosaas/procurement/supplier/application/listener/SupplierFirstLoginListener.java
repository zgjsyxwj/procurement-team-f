package com.cdp.ecosaas.procurement.supplier.application.listener;

import com.cdp.ecosaas.procurement.shared.event.SupplierFirstLoginEvent;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierRepository;
import com.cdp.ecosaas.procurement.supplier.domain.service.SupplierLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 供应商首次登录监听器（任务 8.9）—— 监听模块 01 发布的 {@link SupplierFirstLoginEvent}，
 * 将供应商由「创建成功/待进入」流转为「待完善信息」（Req 7.3）。
 * <p>
 * 同步监听、与登录事务同源（流转失败随登录一并回滚）；为不阻断登录，供应商缺失或已越过该状态时
 * 记录日志后跳过（幂等、防御）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierFirstLoginListener {

    private final SupplierRepository supplierRepository;
    private final SupplierLifecycleService lifecycleService;

    @EventListener
    @Transactional
    public void onSupplierFirstLogin(SupplierFirstLoginEvent event) {
        Supplier supplier = supplierRepository.findById(event.supplierId()).orElse(null);
        if (supplier == null) {
            log.warn("收到供应商首登事件但供应商不存在 supplierId={}", event.supplierId());
            return;
        }
        if (supplier.getStatus() != SupplierStatus.CREATED && supplier.getStatus() != SupplierStatus.PENDING_ENTRY) {
            log.info("供应商 {} 首登事件忽略：当前状态[{}]非待流转",
                    supplier.getId(), supplier.getStatus().getDescription());
            return;
        }
        lifecycleService.markFirstLogin(supplier);   // → 待完善信息
        supplierRepository.save(supplier);
    }
}
