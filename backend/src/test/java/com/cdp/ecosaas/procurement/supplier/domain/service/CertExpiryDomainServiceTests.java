package com.cdp.ecosaas.procurement.supplier.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CertExpiryDomainService 单元测试 —— 剩余天数与提醒节点命中（Req 12.2、12.6）。
 */
class CertExpiryDomainServiceTests {

    private final CertExpiryDomainService service = new CertExpiryDomainService();
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 30);

    @Test
    @DisplayName("daysUntilExpiry 计算剩余天数")
    void shouldComputeDaysUntilExpiry() {
        assertEquals(30, service.daysUntilExpiry(TODAY.plusDays(30), TODAY));
        assertEquals(-1, service.daysUntilExpiry(TODAY.minusDays(1), TODAY));
    }

    @Test
    @DisplayName("命中提醒节点 30/15/7/3/0 返回对应节点")
    void shouldHitReminderNodes() {
        assertEquals(30, service.hitReminderNode(TODAY.plusDays(30), TODAY));
        assertEquals(15, service.hitReminderNode(TODAY.plusDays(15), TODAY));
        assertEquals(7, service.hitReminderNode(TODAY.plusDays(7), TODAY));
        assertEquals(3, service.hitReminderNode(TODAY.plusDays(3), TODAY));
        assertEquals(0, service.hitReminderNode(TODAY, TODAY));
    }

    @Test
    @DisplayName("非提醒节点返回 -1")
    void shouldReturnMinusOneWhenNotOnNode() {
        assertEquals(-1, service.hitReminderNode(TODAY.plusDays(29), TODAY));
        assertEquals(-1, service.hitReminderNode(TODAY.plusDays(1), TODAY));
    }

    @Test
    @DisplayName("hitsAnyReminderNode 判断是否命中提醒节点")
    void shouldDetectAnyNodeHit() {
        assertTrue(service.hitsAnyReminderNode(TODAY.plusDays(7), TODAY));
        assertTrue(service.hitsAnyReminderNode(TODAY, TODAY));
        assertFalse(service.hitsAnyReminderNode(TODAY.plusDays(8), TODAY));
    }
}
