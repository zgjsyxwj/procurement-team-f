package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.supplier.application.command.BuyerAddCertificateCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.ReviewCertificateCommand;
import com.cdp.ecosaas.procurement.supplier.application.command.UploadCertificateCommand;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateAuditStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateSource;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCertificate;
import com.cdp.ecosaas.procurement.supplier.domain.port.FileStoragePort;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierCertificateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CertificateCommandHandler 单元测试 —— 供应商上传、审核、采购员手动添加（Req 10.1-10.4、10.7-10.11）。
 */
class CertificateCommandHandlerTests {

    private final SupplierCertificateRepository certificateRepository = mock(SupplierCertificateRepository.class);
    private final FileStoragePort fileStoragePort = mock(FileStoragePort.class);

    private final CertificateCommandHandler handler =
            new CertificateCommandHandler(certificateRepository, fileStoragePort);

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2027, 1, 1);

    private UploadCertificateCommand uploadCmd() {
        return new UploadCertificateCommand(1L, 100L, null, 1024L, "营业执照.pdf", "application/pdf",
                FROM, TO, null);
    }

    private SupplierCertificate currentValid(Long id, Long certTypeId) {
        return SupplierCertificate.builder().id(id).supplierId(1L).certTypeId(certTypeId)
                .auditStatus(CertificateAuditStatus.APPROVED).source(CertificateSource.SUPPLIER_UPLOAD)
                .isCurrentValid(true).validFrom(FROM).validTo(TO).build();
    }

    @Test
    @DisplayName("供应商上传证件：经OSS上传、置为待审核、来源为供应商上传、当前有效")
    void shouldUploadSupplierCertificateAsPendingReview() {
        when(fileStoragePort.upload(any(), anyLong(), any(), any())).thenReturn("oss-key-1");
        when(certificateRepository.findBySupplierId(1L)).thenReturn(List.of());
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleUpload(uploadCmd(), "供应商用户");

        ArgumentCaptor<SupplierCertificate> captor = ArgumentCaptor.forClass(SupplierCertificate.class);
        verify(certificateRepository).save(captor.capture());
        SupplierCertificate saved = captor.getValue();
        assertEquals("oss-key-1", saved.getFileUrl());
        assertEquals(CertificateAuditStatus.PENDING_REVIEW, saved.getAuditStatus());
        assertEquals(CertificateSource.SUPPLIER_UPLOAD, saved.getSource());
        assertTrue(saved.isCurrentValid());
    }

    @Test
    @DisplayName("上传时有效期截止不晚于起始应拒绝，且不上传不保存")
    void shouldRejectInvalidValidityPeriodOnUpload() {
        UploadCertificateCommand bad = new UploadCertificateCommand(1L, 100L, null, 1024L, "x.pdf",
                "application/pdf", TO, FROM, null);

        assertThrows(IllegalArgumentException.class, () -> handler.handleUpload(bad, "供应商用户"));

        verify(fileStoragePort, never()).upload(any(), anyLong(), any(), any());
        verify(certificateRepository, never()).save(any());
    }

    @Test
    @DisplayName("上传同类证件时，原当前有效版本被置为历史版本")
    void shouldSupersedePreviousCurrentValidOnUpload() {
        when(fileStoragePort.upload(any(), anyLong(), any(), any())).thenReturn("oss-key-2");
        when(certificateRepository.findBySupplierId(1L)).thenReturn(List.of(currentValid(50L, 100L)));
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleUpload(uploadCmd(), "供应商用户");

        ArgumentCaptor<SupplierCertificate> captor = ArgumentCaptor.forClass(SupplierCertificate.class);
        verify(certificateRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        SupplierCertificate old = captor.getAllValues().stream()
                .filter(c -> Long.valueOf(50L).equals(c.getId())).findFirst().orElseThrow();
        assertFalse(old.isCurrentValid());
    }

    @Test
    @DisplayName("审核通过证件：状态变更为已通过")
    void shouldApproveCertificate() {
        SupplierCertificate cert = SupplierCertificate.builder().id(60L).supplierId(1L).certTypeId(100L)
                .auditStatus(CertificateAuditStatus.PENDING_REVIEW).build();
        when(certificateRepository.findById(60L)).thenReturn(Optional.of(cert));
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleReview(new ReviewCertificateCommand(60L, true, null), "审核员");

        ArgumentCaptor<SupplierCertificate> captor = ArgumentCaptor.forClass(SupplierCertificate.class);
        verify(certificateRepository).save(captor.capture());
        assertEquals(CertificateAuditStatus.APPROVED, captor.getValue().getAuditStatus());
    }

    @Test
    @DisplayName("驳回证件：状态变更为驳回并记录原因")
    void shouldRejectCertificateWithReason() {
        SupplierCertificate cert = SupplierCertificate.builder().id(60L).supplierId(1L).certTypeId(100L)
                .auditStatus(CertificateAuditStatus.PENDING_REVIEW).build();
        when(certificateRepository.findById(60L)).thenReturn(Optional.of(cert));
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleReview(new ReviewCertificateCommand(60L, false, "文件不清晰"), "审核员");

        ArgumentCaptor<SupplierCertificate> captor = ArgumentCaptor.forClass(SupplierCertificate.class);
        verify(certificateRepository).save(captor.capture());
        assertEquals(CertificateAuditStatus.REJECTED, captor.getValue().getAuditStatus());
        assertEquals("文件不清晰", captor.getValue().getRejectReason());
    }

    @Test
    @DisplayName("采购员手动添加并更新当前有效：直接已通过、来源采购员维护、置原版本为历史")
    void shouldBuyerAddCertificateAsApprovedAndCurrentValid() {
        when(fileStoragePort.upload(any(), anyLong(), any(), any())).thenReturn("oss-key-3");
        when(certificateRepository.findBySupplierId(1L)).thenReturn(List.of(currentValid(50L, 100L)));
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleBuyerAdd(new BuyerAddCertificateCommand(1L, 100L, null, 1024L, "y.pdf",
                "application/pdf", FROM, TO, null, true), "采购员A");

        ArgumentCaptor<SupplierCertificate> captor = ArgumentCaptor.forClass(SupplierCertificate.class);
        verify(certificateRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        SupplierCertificate added = captor.getAllValues().stream()
                .filter(c -> c.getId() == null).findFirst().orElseThrow();
        assertEquals(CertificateAuditStatus.APPROVED, added.getAuditStatus());
        assertEquals(CertificateSource.BUYER_MAINTAIN, added.getSource());
        assertTrue(added.isCurrentValid());
        SupplierCertificate old = captor.getAllValues().stream()
                .filter(c -> Long.valueOf(50L).equals(c.getId())).findFirst().orElseThrow();
        assertFalse(old.isCurrentValid());
    }

    @Test
    @DisplayName("采购员新增历史证件（不更新当前有效）：新证件为历史版本，不触碰原当前有效证件")
    void shouldBuyerAddHistoricalCertificateWhenNotUpdatingCurrent() {
        when(fileStoragePort.upload(any(), anyLong(), any(), any())).thenReturn("oss-key-4");
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handleBuyerAdd(new BuyerAddCertificateCommand(1L, 100L, null, 1024L, "z.pdf",
                "application/pdf", FROM, TO, null, false), "采购员A");

        // 未更新当前有效 → 不查询既有证件、仅保存新历史证件一次
        verify(certificateRepository, never()).findBySupplierId(anyLong());
        ArgumentCaptor<SupplierCertificate> captor = ArgumentCaptor.forClass(SupplierCertificate.class);
        verify(certificateRepository).save(captor.capture());
        assertFalse(captor.getValue().isCurrentValid());
        assertEquals(CertificateAuditStatus.APPROVED, captor.getValue().getAuditStatus());
    }
}
