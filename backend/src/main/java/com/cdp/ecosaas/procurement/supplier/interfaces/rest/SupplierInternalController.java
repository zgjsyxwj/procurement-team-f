package com.cdp.ecosaas.procurement.supplier.interfaces.rest;

import com.cdp.ecosaas.procurement.supplier.application.handler.SupplierQueryHandler;
import com.cdp.ecosaas.procurement.supplier.interfaces.dto.ActiveSupplierResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部集成接口 —— 供模块 04 询报价获取「合作中」供应商列表。
 */
@RestController
@RequestMapping("/api/internal/suppliers")
@RequiredArgsConstructor
public class SupplierInternalController {

    private final SupplierQueryHandler queryHandler;

    /** 合作中供应商列表。 */
    @GetMapping("/active")
    public List<ActiveSupplierResponse> activeSuppliers() {
        return queryHandler.findActiveSuppliers().stream().map(ActiveSupplierResponse::from).toList();
    }
}
