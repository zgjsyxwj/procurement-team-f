package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateType;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateTypeStatus;
import com.cdp.ecosaas.procurement.supplier.domain.repository.CertificateTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 证件类型查询处理器（任务 8.8/9.7）—— 管理端全量列表与供应商端可选（启用）证件类型。
 */
@Service
@RequiredArgsConstructor
public class CertificateTypeQueryHandler {

    private final CertificateTypeRepository certificateTypeRepository;

    /** 全部证件类型（管理端，Req 11.1）。 */
    public List<CertificateType> findAll() {
        return certificateTypeRepository.findAll();
    }

    /** 启用的证件类型及其差异化字段（供应商上传时可选，Req 11.6）。 */
    public List<CertificateType> findActive() {
        return certificateTypeRepository.findByStatus(CertificateTypeStatus.ACTIVE);
    }
}
