package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;

/**
 * 联系人响应（Req 9.2、9.6）。
 */
public record ContactResponse(Long id, String name, String phone, String email,
                              boolean primary, String position, String department) {

    public static ContactResponse from(SupplierContact c) {
        return new ContactResponse(c.getId(), c.getName(), c.getPhone(), c.getEmail(),
                c.isPrimary(), c.getPosition(), c.getDepartment());
    }
}
