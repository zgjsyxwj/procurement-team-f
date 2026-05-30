package com.cdp.ecosaas.procurement.supplier.application.handler;

import com.cdp.ecosaas.procurement.shared.model.PageResult;
import com.cdp.ecosaas.procurement.supplier.application.query.SupplierListQuery;
import com.cdp.ecosaas.procurement.supplier.application.result.SupplierListItem;
import com.cdp.ecosaas.procurement.supplier.application.service.SupplierAccessService;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertExpiryStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateAuditStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCertificate;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierCertificateRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierContactRepository;
import com.cdp.ecosaas.procurement.supplier.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * 供应商查询处理器（任务 8.8）—— 数据范围内的列表（含主要联系人/证件到期标注）与合作中供应商列表。
 * <p>
 * 数据范围由 {@link SupplierAccessService} 按角色裁剪（ADMIN 全量 / BUYER 管理范围）；
 * 证件到期状态在查询时由证件派生标注（Req 8.1、8.4、12.5）。证件到期状态筛选（Req 8.4）暂未实现。
 */
@Service
@RequiredArgsConstructor
public class SupplierQueryHandler {

    private final SupplierRepository supplierRepository;
    private final SupplierContactRepository contactRepository;
    private final SupplierCertificateRepository certificateRepository;
    private final SupplierAccessService accessService;
    private final Clock clock;

    /** 数据范围内的分页供应商列表，附主要联系人与证件到期标注（Req 8）。 */
    public PageResult<SupplierListItem> search(SupplierListQuery query, String role, Long userId) {
        List<Long> accessibleIds = accessService.accessibleSupplierIds(role, userId);
        if (accessibleIds != null && accessibleIds.isEmpty()) {
            return PageResult.<SupplierListItem>builder()
                    .content(List.of()).page(query.page()).size(query.size())
                    .totalElements(0).totalPages(0).build();
        }

        PageResult<Supplier> page = supplierRepository.search(
                query.nameKeyword(), query.status(), accessibleIds, query.toPageQuery());
        LocalDate today = LocalDate.now(clock);
        List<SupplierListItem> items = page.getContent().stream()
                .map(supplier -> toListItem(supplier, today))
                .toList();
        return PageResult.<SupplierListItem>builder()
                .content(items).page(page.getPage()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).build();
    }

    /** 合作中供应商列表（供模块 04 询报价选择）。 */
    public List<Supplier> findActiveSuppliers() {
        return supplierRepository.findByStatus(SupplierStatus.ACTIVE);
    }

    /** 供应商证件列表（Req 10.5、10）。 */
    public List<SupplierCertificate> listCertificates(Long supplierId) {
        return certificateRepository.findBySupplierId(supplierId);
    }

    // ---------- 私有 ----------

    private SupplierListItem toListItem(Supplier supplier, LocalDate today) {
        SupplierContact primary = contactRepository.findBySupplierId(supplier.getId()).stream()
                .filter(SupplierContact::isPrimary)
                .findFirst()
                .orElse(null);
        return new SupplierListItem(supplier,
                primary == null ? null : primary.getName(),
                primary == null ? null : primary.getPhone(),
                worstExpiryStatus(supplier.getId(), today));
    }

    /** 取当前有效且已通过证件中最严重的到期状态；无则正常（Req 8.4、12.5）。 */
    private CertExpiryStatus worstExpiryStatus(Long supplierId, LocalDate today) {
        CertExpiryStatus worst = CertExpiryStatus.NORMAL;
        for (SupplierCertificate certificate : certificateRepository.findBySupplierId(supplierId)) {
            if (certificate.getAuditStatus() == CertificateAuditStatus.APPROVED && certificate.isCurrentValid()) {
                CertExpiryStatus status = certificate.expiryStatus(today);
                if (severity(status) > severity(worst)) {
                    worst = status;
                }
            }
        }
        return worst;
    }

    private int severity(CertExpiryStatus status) {
        return switch (status) {
            case EXPIRED -> 2;
            case EXPIRING_SOON -> 1;
            case NORMAL -> 0;
        };
    }
}
