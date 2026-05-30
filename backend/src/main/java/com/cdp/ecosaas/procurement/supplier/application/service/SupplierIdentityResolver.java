package com.cdp.ecosaas.procurement.supplier.application.service;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.domain.port.SupplierAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 供应商身份解析（供应商门户端）—— 将登录用户 ID 解析为其所属供应商企业 ID（Req 3.10 本企业数据范围）。
 */
@Service
@RequiredArgsConstructor
public class SupplierIdentityResolver {

    private final SupplierAccountPort accountPort;

    public Long resolveSupplierId(Long userId) {
        return accountPort.findSupplierIdByUserId(userId)
                .orElseThrow(() -> new BusinessException("当前供应商账号未关联企业"));
    }
}
