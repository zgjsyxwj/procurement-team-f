package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 供应商详情响应（企业基本信息 + 银行信息，Req 8.6）。联系人/证件/变更记录由各自接口分别加载。
 */
public record SupplierDetailResponse(Long id, String supplierCode, String name, String category, String status,
                                     String unifiedSocialCreditCode, String legalPerson, LocalDate registeredDate,
                                     BigDecimal registeredCapital, String address, Boolean generalTaxpayer,
                                     String businessScope, String enterpriseNature, String salesMode,
                                     String coverageArea, BigDecimal annualRevenue, Integer employeeCount,
                                     String mainCustomers, List<BankAccountDto> bankAccounts) {

    public static SupplierDetailResponse from(Supplier s) {
        List<BankAccountDto> banks = s.getBankAccounts() == null ? List.of()
                : s.getBankAccounts().stream().map(BankAccountDto::from).toList();
        return new SupplierDetailResponse(
                s.getId(), s.getSupplierCode(), s.getName(),
                s.getCategory() == null ? null : s.getCategory().name(),
                s.getStatus() == null ? null : s.getStatus().name(),
                s.getUnifiedSocialCreditCode(), s.getLegalPerson(), s.getRegisteredDate(),
                s.getRegisteredCapital(), s.getAddress(), s.getGeneralTaxpayer(),
                s.getBusinessScope(), s.getEnterpriseNature(), s.getSalesMode(),
                s.getCoverageArea(), s.getAnnualRevenue(), s.getEmployeeCount(),
                s.getMainCustomers(), banks);
    }
}
