package com.cdp.ecosaas.procurement.supplier.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SupplierCodeGenerator 单元测试 —— VD + 4 位自增编号生成（Req 6.2）。
 */
class SupplierCodeGeneratorTests {

    private final SupplierCodeGenerator generator = new SupplierCodeGenerator();

    @Test
    @DisplayName("生成 VD + 4 位零填充编号")
    void shouldGenerateZeroPaddedCode() {
        assertEquals("VD0001", generator.generate(1));
        assertEquals("VD0042", generator.generate(42));
        assertEquals("VD9999", generator.generate(9999));
    }

    @Test
    @DisplayName("序号超过 9999 时自然扩展位数")
    void shouldExpandBeyondFourDigits() {
        assertEquals("VD10000", generator.generate(10000));
    }

    @Test
    @DisplayName("非正序号应抛异常")
    void shouldRejectNonPositiveSequence() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(0));
    }
}
