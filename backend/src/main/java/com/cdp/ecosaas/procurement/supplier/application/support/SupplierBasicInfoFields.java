package com.cdp.ecosaas.procurement.supplier.application.support;

import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * 供应商基本信息可编辑字段注册表（任务 8.2，BASIC_INFO）。
 * <p>
 * 集中维护「字段 key ↔ 显示名 ↔ 读取（格式化为字符串）↔ 应用（解析字符串回填 builder）」，
 * 供变更差异计算与审核通过/直接编辑时的「重建并保存」复用，避免在多处散落字段映射。
 * 银行等多值字段不在此（随后单独处理）。
 */
public final class SupplierBasicInfoFields {

    private SupplierBasicInfoFields() {
    }

    private record Field(String key, String label,
                         Function<Supplier, String> read,
                         BiConsumer<Supplier.SupplierBuilder, String> write) {
    }

    private static final List<Field> FIELDS = List.of(
            new Field("name", "供应商名称", Supplier::getName, Supplier.SupplierBuilder::name),
            new Field("unifiedSocialCreditCode", "统一社会信用代码",
                    Supplier::getUnifiedSocialCreditCode, Supplier.SupplierBuilder::unifiedSocialCreditCode),
            new Field("legalPerson", "公司法人", Supplier::getLegalPerson, Supplier.SupplierBuilder::legalPerson),
            new Field("registeredDate", "注册时间",
                    s -> fmt(s.getRegisteredDate()), (b, v) -> b.registeredDate(parseDate(v))),
            new Field("registeredCapital", "注册资金",
                    s -> fmt(s.getRegisteredCapital()), (b, v) -> b.registeredCapital(parseDecimal(v))),
            new Field("address", "公司地址", Supplier::getAddress, Supplier.SupplierBuilder::address),
            new Field("generalTaxpayer", "一般纳税人",
                    s -> fmt(s.getGeneralTaxpayer()), (b, v) -> b.generalTaxpayer(parseBool(v))),
            new Field("businessScope", "经营范围", Supplier::getBusinessScope, Supplier.SupplierBuilder::businessScope),
            new Field("enterpriseNature", "企业性质", Supplier::getEnterpriseNature, Supplier.SupplierBuilder::enterpriseNature),
            new Field("salesMode", "销售模式", Supplier::getSalesMode, Supplier.SupplierBuilder::salesMode),
            new Field("coverageArea", "覆盖区域", Supplier::getCoverageArea, Supplier.SupplierBuilder::coverageArea),
            new Field("annualRevenue", "本年度营业额",
                    s -> fmt(s.getAnnualRevenue()), (b, v) -> b.annualRevenue(parseDecimal(v))),
            new Field("employeeCount", "员工人数",
                    s -> fmt(s.getEmployeeCount()), (b, v) -> b.employeeCount(parseInt(v))),
            new Field("mainCustomers", "主力客户", Supplier::getMainCustomers, Supplier.SupplierBuilder::mainCustomers)
    );

    private static final Map<String, Field> BY_KEY = FIELDS.stream().collect(toMap(Field::key, f -> f));

    /** 当前供应商基本信息快照（key -> 字符串值，含 null）。 */
    public static Map<String, String> snapshot(Supplier supplier) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Field f : FIELDS) {
            map.put(f.key(), f.read().apply(supplier));
        }
        return map;
    }

    /** 字段显示名（key -> label），供差异展示。 */
    public static Map<String, String> labels() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Field f : FIELDS) {
            map.put(f.key(), f.label());
        }
        return map;
    }

    /** 是否为已登记的可编辑基本信息字段。 */
    public static boolean isKnownField(String key) {
        return BY_KEY.containsKey(key);
    }

    /**
     * 将变更明细的 afterValue 应用到当前供应商，返回重建后的供应商（保持聚合封装，不暴露通用 setter）。
     * 未登记的字段 key 忽略。
     */
    public static Supplier applyChanges(Supplier current, List<SupplierChangeField> changes) {
        Supplier.SupplierBuilder builder = current.toBuilder();
        for (SupplierChangeField change : changes) {
            Field field = BY_KEY.get(change.getFieldKey());
            if (field != null) {
                field.write().accept(builder, change.getAfterValue());
            }
        }
        return builder.build();
    }

    // ---------- 格式化 / 解析 ----------

    private static String fmt(LocalDate value) {
        return value == null ? null : value.toString();
    }

    private static String fmt(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    private static String fmt(Boolean value) {
        return value == null ? null : value.toString();
    }

    private static String fmt(Integer value) {
        return value == null ? null : value.toString();
    }

    private static LocalDate parseDate(String value) {
        return isBlank(value) ? null : LocalDate.parse(value.trim());
    }

    private static BigDecimal parseDecimal(String value) {
        return isBlank(value) ? null : new BigDecimal(value.trim());
    }

    private static Boolean parseBool(String value) {
        return isBlank(value) ? null : Boolean.valueOf(value.trim());
    }

    private static Integer parseInt(String value) {
        return isBlank(value) ? null : Integer.valueOf(value.trim());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
