package com.cdp.ecosaas.procurement.supplier.application.query;

import com.cdp.ecosaas.procurement.shared.model.PageQuery;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;

/**
 * 供应商列表查询（Req 8.1-8.3、8.5）。
 *
 * @param nameKeyword 企业名称模糊关键字（null/空白不筛选）
 * @param status      状态筛选（null 不筛选）
 * @param page        页码（从 0 开始）
 * @param size        每页大小
 */
public record SupplierListQuery(String nameKeyword, SupplierStatus status, int page, int size) {

    public PageQuery toPageQuery() {
        return new PageQuery(page, size);
    }
}
