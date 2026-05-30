package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.Getter;

/**
 * 供应商信息变更申请状态枚举（Req 3.3、3.7、5.3、5.4）。
 */
@Getter
public enum ChangeRequestStatus {

    PENDING_REVIEW("待审核"),
    APPROVED("已通过"),
    REJECTED("驳回"),
    WITHDRAWN("已撤回");

    private final String description;

    ChangeRequestStatus(String description) {
        this.description = description;
    }
}
