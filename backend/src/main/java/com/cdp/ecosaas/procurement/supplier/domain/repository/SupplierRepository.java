package com.cdp.ecosaas.procurement.supplier.domain.repository;

import com.cdp.ecosaas.procurement.shared.model.PageQuery;
import com.cdp.ecosaas.procurement.shared.model.PageResult;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 供应商仓储接口（领域层端口），由基础设施层实现（任务 5.3）。
 * <p>
 * 证件到期状态为派生属性，不在此过滤——列表的到期状态标注/筛选由查询层（任务 8.8）结合证件数据完成。
 */
public interface SupplierRepository {

    Supplier save(Supplier supplier);

    Optional<Supplier> findById(Long id);

    Optional<Supplier> findByCode(String supplierCode);

    /**
     * 分页搜索：按企业名称模糊 + 状态筛选 + 数据范围限定（Req 8.1-8.3、8.5、50.5）。
     *
     * @param nameKeyword          名称模糊关键字，null/空白不筛选
     * @param status               状态筛选，null 不筛选
     * @param accessibleSupplierIds 可见供应商 ID 范围，{@code null} 表示不受限（ADMIN）；非空集合按其限定
     */
    PageResult<Supplier> search(String nameKeyword, SupplierStatus status,
                                Collection<Long> accessibleSupplierIds, PageQuery pageQuery);

    /**
     * 按状态查询（供模块 04 获取「合作中」供应商列表）。
     */
    List<Supplier> findByStatus(SupplierStatus status);

    /**
     * 获取下一个供应商编号自增序号（用于生成 VD+4 位编号，Req 6.2）。
     */
    long nextCodeSequence();
}
