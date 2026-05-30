package com.cdp.ecosaas.procurement.supplier.domain.service;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.shared.exception.PrimaryContactRequiredException;

import java.util.List;
import java.util.Objects;

/**
 * 联系人领域服务 —— 主要联系人唯一性与删除约束（Req 9.3、9.4、9.5）。
 * 无状态、不依赖 Spring。
 */
public class ContactDomainService {

    /**
     * 将指定联系人设为主要联系人，并自动取消其余联系人的主要标记（Req 9.4）。
     *
     * @throws IllegalArgumentException 目标联系人不在列表中
     */
    public void setPrimary(List<SupplierContact> contacts, Long targetContactId) {
        boolean found = false;
        for (SupplierContact contact : contacts) {
            if (Objects.equals(contact.getId(), targetContactId)) {
                contact.markPrimary();
                found = true;
            } else {
                contact.unmarkPrimary();
            }
        }
        if (!found) {
            throw new IllegalArgumentException("指定的联系人不存在");
        }
    }

    /**
     * 校验删除约束：不可删除主要联系人（需先指定其他联系人为主要联系人，Req 9.5）。
     *
     * @throws IllegalArgumentException        目标联系人不在列表中
     * @throws PrimaryContactRequiredException 目标为主要联系人
     */
    public void ensureCanDelete(List<SupplierContact> contacts, Long contactId) {
        SupplierContact target = contacts.stream()
                .filter(contact -> Objects.equals(contact.getId(), contactId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("指定的联系人不存在"));
        if (target.isPrimary()) {
            throw new PrimaryContactRequiredException("不能删除主要联系人，请先指定其他联系人为主要联系人");
        }
    }

    /**
     * 校验供应商至少存在一个主要联系人（Req 9.3）。
     *
     * @throws PrimaryContactRequiredException 无主要联系人
     */
    public void ensureHasPrimary(List<SupplierContact> contacts) {
        boolean hasPrimary = contacts.stream().anyMatch(SupplierContact::isPrimary);
        if (!hasPrimary) {
            throw new PrimaryContactRequiredException("每个供应商至少需要一个主要联系人");
        }
    }
}
