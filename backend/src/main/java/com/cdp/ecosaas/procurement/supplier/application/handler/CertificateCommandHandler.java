package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.exception.BusinessException;
import com.cdp.ecosaas.procurement.supplier.application.command.BuyerAddCertificateCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.ReviewCertificateCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.UploadCertificateCommand;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateAuditStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateSource;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCertificate;
import com.cdp.ecosaas.procurement.supplier.domain.port.FileStoragePort;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 证件命令处理器（任务 8.6）—— 供应商上传（待审核）、采购员审核（通过/驳回）、采购员手动添加（直接已通过）。
 * <p>
 * 文件经 {@link FileStoragePort} 存入 OSS（真实 COS 接入延后，单测以 mock 注入）；
 * 通过 {@code isCurrentValid} 区分当前有效版本与历史版本（Req 10.11）。
 * 证件驳回通知供应商（Req 10.8 的邮件）按决策暂不实现。
 */
@Service
@RequiredArgsConstructor
public class CertificateCommandHandler {

    private final SupplierCertificateRepository certificateRepository;
    private final FileStoragePort fileStoragePort;

    /** 供应商上传/更新证件 → 待审核；同类原当前有效版本置为历史（Req 10.1-10.4、10.11）。 */
    @Transactional
    public SupplierCertificate handleUpload(UploadCertificateCommand cmd, String maintainedBy) {
        requireValidPeriod(cmd.validFrom(), cmd.validTo());
        String fileUrl = fileStoragePort.upload(cmd.content(), cmd.contentLength(), cmd.fileName(), cmd.contentType());

        supersedeCurrentValid(cmd.supplierId(), cmd.certTypeId());

        return certificateRepository.save(SupplierCertificate.builder()
                .supplierId(cmd.supplierId())
                .certTypeId(cmd.certTypeId())
                .fileUrl(fileUrl)
                .fileName(cmd.fileName())
                .validFrom(cmd.validFrom())
                .validTo(cmd.validTo())
                .auditStatus(CertificateAuditStatus.PENDING_REVIEW)
                .source(CertificateSource.SUPPLIER_UPLOAD)
                .isCurrentValid(true)
                .extraFields(cmd.extraFields())
                .maintainedBy(maintainedBy)
                .build());
    }

    /** 采购员审核证件：通过 → 已通过；驳回 → 驳回并记录原因（Req 10.7、10.8）。 */
    @Transactional
    public void handleReview(ReviewCertificateCommand cmd, String reviewer) {
        SupplierCertificate certificate = certificateRepository.findById(cmd.certificateId())
                .orElseThrow(() -> new BusinessException("证件不存在：" + cmd.certificateId()));
        if (cmd.approve()) {
            certificate.approve();
        } else {
            certificate.reject(cmd.reason());
        }
        certificateRepository.save(certificate);
        // Req 10.8 驳回通知供应商（邮件）按决策暂不实现。
    }

    /** 采购员手动添加证件 → 直接已通过、不进审核中心；可更新当前有效或新增历史版本（Req 10.9、10.11）。 */
    @Transactional
    public SupplierCertificate handleBuyerAdd(BuyerAddCertificateCommand cmd, String maintainedBy) {
        requireValidPeriod(cmd.validFrom(), cmd.validTo());
        String fileUrl = fileStoragePort.upload(cmd.content(), cmd.contentLength(), cmd.fileName(), cmd.contentType());

        if (cmd.updateCurrentValid()) {
            supersedeCurrentValid(cmd.supplierId(), cmd.certTypeId());
        }

        return certificateRepository.save(SupplierCertificate.builder()
                .supplierId(cmd.supplierId())
                .certTypeId(cmd.certTypeId())
                .fileUrl(fileUrl)
                .fileName(cmd.fileName())
                .validFrom(cmd.validFrom())
                .validTo(cmd.validTo())
                .auditStatus(CertificateAuditStatus.APPROVED)
                .source(CertificateSource.BUYER_MAINTAIN)
                .isCurrentValid(cmd.updateCurrentValid())
                .extraFields(cmd.extraFields())
                .maintainedBy(maintainedBy)
                .build());
    }

    // ---------- 私有 ----------

    /** 将供应商同一证件类型的原当前有效版本置为历史版本（Req 10.11）。 */
    private void supersedeCurrentValid(Long supplierId, Long certTypeId) {
        certificateRepository.findBySupplierId(supplierId).stream()
                .filter(c -> Objects.equals(c.getCertTypeId(), certTypeId) && c.isCurrentValid())
                .forEach(c -> {
                    c.markHistorical();
                    certificateRepository.save(c);
                });
    }

    private void requireValidPeriod(LocalDate validFrom, LocalDate validTo) {
        // 复用聚合根的有效期校验（Req 10.2），在上传前拦截，避免无效日期文件入库。
        SupplierCertificate.builder().validFrom(validFrom).validTo(validTo).build().validateValidityPeriod();
    }
}
