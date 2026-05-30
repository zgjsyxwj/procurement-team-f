package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.model.PageResult;
import com.cdp.ecosaas.procurement.supplier.application.query.SupplierListQuery;
import com.cdp.ecosaas.procurement.supplier.application.result.SupplierListItem;
import com.cdp.ecosaas.procurement.supplier.application.service.SupplierAccessService;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertExpiryStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateAuditStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateSource;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCategory;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCertificate;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierCertificateRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierContactRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SupplierQueryHandler 单元测试 —— 数据范围列表、主要联系人/证件到期标注、合作中列表（Req 8）。
 */
class SupplierQueryHandlerTests {

    private final SupplierRepository supplierRepository = mock(SupplierRepository.class);
    private final SupplierContactRepository contactRepository = mock(SupplierContactRepository.class);
    private final SupplierCertificateRepository certificateRepository = mock(SupplierCertificateRepository.class);
    private final SupplierAccessService accessService = mock(SupplierAccessService.class);

    // 固定今日为 2026-06-01，便于证件到期断言
    private final Clock clock = Clock.fixed(
            LocalDate.of(2026, 6, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    private final SupplierQueryHandler handler = new SupplierQueryHandler(
            supplierRepository, contactRepository, certificateRepository, accessService, clock);

    private Supplier supplier(Long id) {
        return Supplier.builder().id(id).supplierCode("VD000" + id).name("供应商" + id)
                .category(SupplierCategory.DOMESTIC).status(SupplierStatus.ACTIVE).build();
    }

    private SupplierCertificate cert(LocalDate validTo, CertificateAuditStatus status, boolean currentValid) {
        return SupplierCertificate.builder().id(1L).supplierId(10L).certTypeId(100L)
                .validFrom(LocalDate.of(2025, 1, 1)).validTo(validTo)
                .auditStatus(status).source(CertificateSource.SUPPLIER_UPLOAD).isCurrentValid(currentValid).build();
    }

    private SupplierListQuery query() {
        return new SupplierListQuery(null, null, 0, 10);
    }

    @Test
    @DisplayName("BUYER 无可见供应商时返回空页，不查询仓储")
    void shouldReturnEmptyWhenBuyerHasNoAccessibleSuppliers() {
        when(accessService.accessibleSupplierIds("BUYER", 7L)).thenReturn(List.of());

        PageResult<SupplierListItem> result = handler.search(query(), "BUYER", 7L);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(supplierRepository, never()).search(any(), any(), any(), any());
    }

    @Test
    @DisplayName("ADMIN 列表：不受限搜索，附主要联系人与证件到期标注（即将到期）")
    void shouldListSuppliersWithPrimaryContactAndCertExpiryForAdmin() {
        when(accessService.accessibleSupplierIds("ADMIN", 1L)).thenReturn(null);
        when(supplierRepository.search(isNull(), isNull(), isNull(), any())).thenReturn(
                PageResult.<Supplier>builder().content(List.of(supplier(10L)))
                        .page(0).size(10).totalElements(1).totalPages(1).build());
        when(contactRepository.findBySupplierId(10L)).thenReturn(List.of(SupplierContact.builder()
                .id(50L).supplierId(10L).name("主要联系人").phone("13800138000").email("p@test.com")
                .isPrimary(true).build()));
        // 截止日 2026-06-20 距今 2026-06-01 不足 30 天 → 即将到期
        when(certificateRepository.findBySupplierId(10L)).thenReturn(List.of(
                cert(LocalDate.of(2026, 6, 20), CertificateAuditStatus.APPROVED, true)));

        PageResult<SupplierListItem> result = handler.search(query(), "ADMIN", 1L);

        assertEquals(1, result.getContent().size());
        SupplierListItem item = result.getContent().get(0);
        assertEquals("主要联系人", item.primaryContactName());
        assertEquals("13800138000", item.primaryContactPhone());
        assertEquals(CertExpiryStatus.EXPIRING_SOON, item.certExpiryStatus());
    }

    @Test
    @DisplayName("证件到期标注取最严重者：含已过期证件 → 已过期；忽略非当前有效/未通过")
    void shouldAnnotateWithMostSevereExpiryStatus() {
        when(accessService.accessibleSupplierIds("ADMIN", 1L)).thenReturn(null);
        when(supplierRepository.search(any(), any(), isNull(), any())).thenReturn(
                PageResult.<Supplier>builder().content(List.of(supplier(10L)))
                        .page(0).size(10).totalElements(1).totalPages(1).build());
        when(contactRepository.findBySupplierId(10L)).thenReturn(List.of());
        when(certificateRepository.findBySupplierId(10L)).thenReturn(List.of(
                cert(LocalDate.of(2030, 1, 1), CertificateAuditStatus.APPROVED, true),     // 正常
                cert(LocalDate.of(2025, 1, 1), CertificateAuditStatus.APPROVED, true),     // 已过期
                cert(LocalDate.of(2025, 1, 1), CertificateAuditStatus.APPROVED, false)));  // 历史版本，忽略

        PageResult<SupplierListItem> result = handler.search(query(), "ADMIN", 1L);

        assertEquals(CertExpiryStatus.EXPIRED, result.getContent().get(0).certExpiryStatus());
    }

    @Test
    @DisplayName("合作中供应商列表：委托仓储按状态查询")
    void shouldFindActiveSuppliers() {
        when(supplierRepository.findByStatus(SupplierStatus.ACTIVE)).thenReturn(List.of(supplier(10L), supplier(11L)));

        assertEquals(2, handler.findActiveSuppliers().size());
        verify(supplierRepository).findByStatus(eq(SupplierStatus.ACTIVE));
    }
}
