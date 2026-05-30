package com.cdp.ecosaas.procurement.supplier.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SupplierCertificate 实体单元测试 —— 审核流转、到期状态派生、有效期校验（Req 10.2、10.7、10.8、12.6）。
 */
class SupplierCertificateTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 30);

    private SupplierCertificate certWithValidTo(LocalDate validTo) {
        return SupplierCertificate.builder()
                .id(1L)
                .supplierId(1L)
                .certTypeId(1L)
                .fileUrl("oss://cert/1")
                .fileName("营业执照.pdf")
                .validFrom(LocalDate.of(2025, 1, 1))
                .validTo(validTo)
                .auditStatus(CertificateAuditStatus.PENDING_REVIEW)
                .source(CertificateSource.SUPPLIER_UPLOAD)
                .isCurrentValid(true)
                .build();
    }

    @Nested
    @DisplayName("expiryStatus - 到期状态派生")
    class ExpiryStatusTests {

        @Test
        @DisplayName("截止日在未来30天以外为正常")
        void shouldBeNormalWhenFarFromExpiry() {
            SupplierCertificate cert = certWithValidTo(TODAY.plusDays(31));

            assertEquals(CertExpiryStatus.NORMAL, cert.expiryStatus(TODAY));
        }

        @Test
        @DisplayName("截止日恰好30天后为即将到期")
        void shouldBeExpiringSoonAt30Days() {
            SupplierCertificate cert = certWithValidTo(TODAY.plusDays(30));

            assertEquals(CertExpiryStatus.EXPIRING_SOON, cert.expiryStatus(TODAY));
        }

        @Test
        @DisplayName("截止日为当天为即将到期")
        void shouldBeExpiringSoonOnDueDate() {
            SupplierCertificate cert = certWithValidTo(TODAY);

            assertEquals(CertExpiryStatus.EXPIRING_SOON, cert.expiryStatus(TODAY));
        }

        @Test
        @DisplayName("截止日已过为已过期")
        void shouldBeExpiredWhenPastDueDate() {
            SupplierCertificate cert = certWithValidTo(TODAY.minusDays(1));

            assertEquals(CertExpiryStatus.EXPIRED, cert.expiryStatus(TODAY));
        }
    }

    @Nested
    @DisplayName("approve / reject - 审核流转")
    class AuditTests {

        @Test
        @DisplayName("审核通过应置为已通过")
        void shouldSetApprovedOnApprove() {
            SupplierCertificate cert = certWithValidTo(TODAY.plusDays(100));

            cert.approve();

            assertEquals(CertificateAuditStatus.APPROVED, cert.getAuditStatus());
        }

        @Test
        @DisplayName("审核驳回应置为驳回并记录原因")
        void shouldSetRejectedWithReasonOnReject() {
            SupplierCertificate cert = certWithValidTo(TODAY.plusDays(100));

            cert.reject("证件模糊不清");

            assertEquals(CertificateAuditStatus.REJECTED, cert.getAuditStatus());
            assertEquals("证件模糊不清", cert.getRejectReason());
        }
    }

    @Nested
    @DisplayName("validateValidityPeriod - 有效期校验（Req 10.2）")
    class ValidityPeriodTests {

        @Test
        @DisplayName("截止日晚于起始日应校验通过")
        void shouldPassWhenValidToAfterValidFrom() {
            SupplierCertificate cert = certWithValidTo(LocalDate.of(2027, 1, 1));

            assertDoesNotThrow(cert::validateValidityPeriod);
        }

        @Test
        @DisplayName("截止日不晚于起始日应抛出异常")
        void shouldThrowWhenValidToNotAfterValidFrom() {
            SupplierCertificate cert = certWithValidTo(LocalDate.of(2024, 1, 1));

            assertThrows(IllegalArgumentException.class, cert::validateValidityPeriod);
        }
    }
}
