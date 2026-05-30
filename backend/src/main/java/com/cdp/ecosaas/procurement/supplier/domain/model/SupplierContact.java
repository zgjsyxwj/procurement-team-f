package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 供应商联系人实体。
 * <p>
 * 联系人不区分销售/财务类型（Req 6.6、9.1）。本实体仅负责自身的主要联系人标记；
 * 跨联系人的「每个供应商至多一个主要联系人」约束由 ContactDomainService（任务 3.5）负责。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierContact {

    private Long id;
    private Long supplierId;
    private String name;        // 姓名(必填)
    private String phone;       // 手机号(必填)
    private String email;       // 邮箱(必填)
    private boolean isPrimary;  // 是否主要联系人
    private String position;    // 职务(选填)
    private String department;  // 部门(选填)

    /**
     * 标记为主要联系人（Req 9.4）。
     */
    public void markPrimary() {
        this.isPrimary = true;
    }

    /**
     * 取消主要联系人标记（设主时由领域服务取消原主要联系人）。
     */
    public void unmarkPrimary() {
        this.isPrimary = false;
    }
}
