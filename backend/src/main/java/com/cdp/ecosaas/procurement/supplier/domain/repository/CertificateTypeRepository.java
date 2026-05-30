package com.cdp.ecosaas.procurement.supplier.domain.repository;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateType;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateTypeStatus;

import java.util.List;
import java.util.Optional;

/**
 * 证件类型字典仓储接口（领域层端口），由基础设施层实现（任务 5.3）。
 */
public interface CertificateTypeRepository {

    CertificateType save(CertificateType certificateType);

    Optional<CertificateType> findById(Long id);

    List<CertificateType> findAll();

    /**
     * 按状态查询（供应商上传时仅展示启用的证件类型，Req 11.4、11.6）。
     */
    List<CertificateType> findByStatus(CertificateTypeStatus status);

    /**
     * 证件类型名称是否已存在（名称唯一校验，Req 11.3）。
     */
    boolean existsByName(String name);
}
