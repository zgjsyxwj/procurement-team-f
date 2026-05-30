package com.cdp.ecosaas.procurement.supplier.domain.service;

import com.cdp.ecosaas.procurement.supplier.shared.constants.SupplierConstants;

/**
 * 供应商编号生成器 —— 规则 VD + 4 位自增序号（Req 6.2）。
 * <p>
 * 自增序号来源（数据库序列）由仓储提供（{@code SupplierRepository#nextCodeSequence}）；
 * 本服务仅负责格式化，无状态、不依赖 Spring。
 */
public class SupplierCodeGenerator {

    private static final String PREFIX = SupplierConstants.CODE_PREFIX;

    /**
     * 生成供应商编号：{@code VD} + 至少 4 位零填充序号（序号超过 9999 时自然扩展位数）。
     *
     * @throws IllegalArgumentException 序号非正数
     */
    public String generate(long sequence) {
        if (sequence < 1) {
            throw new IllegalArgumentException("供应商编号序号必须为正数");
        }
        return PREFIX + String.format("%04d", sequence);
    }
}
