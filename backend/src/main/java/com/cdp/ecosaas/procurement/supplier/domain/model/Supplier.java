package com.cdp.ecosaas.procurement.supplier.domain.model;

import com.cdp.ecosaas.procurement.supplier.shared.exception.InvalidSupplierStatusException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 供应商企业聚合根。
 * <p>
 * 承载企业基本信息、银行信息与生命周期状态机。状态流转集中由本聚合根校验，
 * 非法流转抛 {@link InvalidSupplierStatusException}（Req 7）。
 * 纯领域模型，不含 JPA 注解或 Spring 依赖；审计字段（createdAt 等）与乐观锁版本由持久化层承载。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Supplier {

    private Long id;
    private String supplierCode;              // 供应商ID号(VD+4位)
    private String name;                      // 供应商名称
    private SupplierCategory category;        // 分类
    private SupplierStatus status;            // 生命周期状态

    private String unifiedSocialCreditCode;   // 统一社会信用代码
    private String legalPerson;               // 公司法人
    private LocalDate registeredDate;         // 注册时间
    private BigDecimal registeredCapital;     // 注册资金
    private String address;                   // 公司地址
    private Boolean generalTaxpayer;          // 一般纳税人

    // 选填字段
    private String businessScope;             // 经营范围
    private String enterpriseNature;          // 企业性质
    private String salesMode;                 // 销售模式
    private String coverageArea;              // 覆盖区域
    private BigDecimal annualRevenue;         // 本年度营业额
    private Integer employeeCount;            // 员工人数
    private String mainCustomers;             // 主力客户

    private List<SupplierBankAccount> bankAccounts;

    // ----------------------------- 状态机（Req 7） -----------------------------

    /**
     * 发送/重发邀请：创建成功/待进入 → 待进入（Req 6.7、7.2）。
     */
    public void invite() {
        requireStatus("发送邀请", SupplierStatus.CREATED, SupplierStatus.PENDING_ENTRY);
        this.status = SupplierStatus.PENDING_ENTRY;
    }

    /**
     * 供应商首次登录：创建成功/待进入 → 待完善信息（Req 7.3）。
     */
    public void onFirstLogin() {
        requireStatus("首次登录流转", SupplierStatus.CREATED, SupplierStatus.PENDING_ENTRY);
        this.status = SupplierStatus.PENDING_INFO;
    }

    /**
     * 提交准入审核：待完善信息 → 待审核信息（Req 4.4、7.4）。
     */
    public void submitForReview() {
        requireStatus("提交审核", SupplierStatus.PENDING_INFO);
        this.status = SupplierStatus.PENDING_REVIEW;
    }

    /**
     * 审核通过：待审核信息 → 合作中（Req 5.3、7.5）。
     */
    public void approve() {
        requireStatus("审核通过", SupplierStatus.PENDING_REVIEW);
        this.status = SupplierStatus.ACTIVE;
    }

    /**
     * 审核驳回：待审核信息 → 待完善信息（Req 5.4、7.6）。
     */
    public void reject() {
        requireStatus("审核驳回", SupplierStatus.PENDING_REVIEW);
        this.status = SupplierStatus.PENDING_INFO;
    }

    /**
     * 手动设为合作中：待完善信息/待审核信息 → 合作中（Req 7.9）。
     */
    public void activate() {
        requireStatus("设为合作中", SupplierStatus.PENDING_INFO, SupplierStatus.PENDING_REVIEW);
        this.status = SupplierStatus.ACTIVE;
    }

    /**
     * 停用：除已停用外的任意状态 → 已停用（Req 7.7、7.11）。
     */
    public void disable() {
        requireStatus("停用", SupplierStatus.CREATED, SupplierStatus.PENDING_ENTRY,
                SupplierStatus.PENDING_INFO, SupplierStatus.PENDING_REVIEW, SupplierStatus.ACTIVE);
        this.status = SupplierStatus.DISABLED;
    }

    /**
     * 重新启用：已停用 → 合作中（Req 7.8）。
     */
    public void enable() {
        requireStatus("重新启用", SupplierStatus.DISABLED);
        this.status = SupplierStatus.ACTIVE;
    }

    /**
     * 是否可参与报价：仅「合作中」（Req 4.5、4.6）。
     */
    public boolean canQuote() {
        return this.status == SupplierStatus.ACTIVE;
    }

    private void requireStatus(String action, SupplierStatus... allowed) {
        for (SupplierStatus allowedStatus : allowed) {
            if (this.status == allowedStatus) {
                return;
            }
        }
        throw new InvalidSupplierStatusException(
                "当前状态[" + (status == null ? "未知" : status.getDescription()) + "]不允许执行「" + action + "」操作");
    }
}
