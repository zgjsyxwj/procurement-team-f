package com.cdp.ecosaas.procurement.supplier.domain.service;

import com.cdp.ecosaas.procurement.supplier.shared.constants.SupplierConstants;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 证件到期领域服务 —— 剩余天数与提醒节点命中计算（Req 12.2、12.6）。
 * 无状态、不依赖 Spring。
 */
public class CertExpiryDomainService {

    /**
     * 提醒节点（距截止日的天数）：30/15/7/3/0，来源 {@link SupplierConstants#CERT_EXPIRY_REMINDER_NODES}（任务 17.1）。
     */
    public static final int[] REMINDER_NODES = SupplierConstants.CERT_EXPIRY_REMINDER_NODES;

    /**
     * 距到期的剩余天数（{@code validTo - today}），已过期为负数。
     */
    public long daysUntilExpiry(LocalDate validTo, LocalDate today) {
        return ChronoUnit.DAYS.between(today, validTo);
    }

    /**
     * 命中的提醒节点：剩余天数恰为 30/15/7/3/0 之一时返回该节点，否则返回 -1。
     */
    public int hitReminderNode(LocalDate validTo, LocalDate today) {
        long days = daysUntilExpiry(validTo, today);
        for (int node : REMINDER_NODES) {
            if (days == node) {
                return node;
            }
        }
        return -1;
    }

    /**
     * 是否命中任一提醒节点。
     */
    public boolean hitsAnyReminderNode(LocalDate validTo, LocalDate today) {
        return hitReminderNode(validTo, today) >= 0;
    }
}
