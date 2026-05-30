package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.command.ChangeCertTypeStatusCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.SaveCertTypeCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.UpdateCertTypeFieldsCommand;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateType;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateTypeStatus;
import com.cdp.ecosaas.procurement.supplier.domain.repository.CertificateTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 证件类型字典命令处理器（任务 8.7）—— 新增/编辑、启用/停用、差异化字段维护。
 * <p>
 * 名称唯一（Req 11.3）；停用保留历史证件数据，仅新上传时不再展示（Req 11.4）；
 * 差异化字段整体替换（Req 11.5）。
 */
@Service
@RequiredArgsConstructor
public class CertificateTypeCommandHandler {

    private final CertificateTypeRepository certificateTypeRepository;

    /** 新增/编辑证件类型；名称必填、唯一（Req 11.1-11.3）。 */
    @Transactional
    public CertificateType handleSave(SaveCertTypeCommand cmd) {
        if (cmd.name() == null || cmd.name().isBlank()) {
            throw new BusinessException("证件类型名称必填");
        }
        if (cmd.id() == null) {
            if (certificateTypeRepository.existsByName(cmd.name())) {
                throw new BusinessException("证件类型名称已存在：" + cmd.name());
            }
            return certificateTypeRepository.save(CertificateType.builder()
                    .name(cmd.name())
                    .status(CertificateTypeStatus.ACTIVE)
                    .remark(cmd.remark())
                    .build());
        }

        CertificateType existing = loadCertType(cmd.id());
        if (!cmd.name().equals(existing.getName()) && certificateTypeRepository.existsByName(cmd.name())) {
            throw new BusinessException("证件类型名称已存在：" + cmd.name());
        }
        return certificateTypeRepository.save(CertificateType.builder()
                .id(existing.getId())
                .name(cmd.name())
                .status(existing.getStatus())
                .remark(cmd.remark())
                .fields(existing.getFields())
                .build());
    }

    /** 维护差异化字段（整体替换，Req 11.5）。 */
    @Transactional
    public CertificateType handleUpdateFields(UpdateCertTypeFieldsCommand cmd) {
        CertificateType existing = loadCertType(cmd.certTypeId());
        return certificateTypeRepository.save(CertificateType.builder()
                .id(existing.getId())
                .name(existing.getName())
                .status(existing.getStatus())
                .remark(existing.getRemark())
                .fields(cmd.fields())
                .build());
    }

    /** 启用/停用证件类型（Req 11.1、11.4）。 */
    @Transactional
    public void handleChangeStatus(ChangeCertTypeStatusCommand cmd) {
        CertificateType existing = loadCertType(cmd.certTypeId());
        if (cmd.active()) {
            existing.enable();
        } else {
            existing.disable();
        }
        certificateTypeRepository.save(existing);
    }

    private CertificateType loadCertType(Long id) {
        return certificateTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("证件类型不存在：" + id));
    }
}
