package com.cdp.ecosaas.procurement.supplier.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 供应商银行账号值对象。
 * <p>
 * 银行信息整体非必填，但一旦填写一条记录，则户名、开户银行名称、银行账号三者必填（Req 3.9）。
 * 纯领域对象，不含 JPA 注解。
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierBankAccount {

    private String accountName;    // 户名
    private String bankName;       // 开户银行名称
    private String accountNumber;  // 银行账号

    /**
     * 三项是否齐全。
     */
    public boolean isComplete() {
        return notBlank(accountName) && notBlank(bankName) && notBlank(accountNumber);
    }

    /**
     * 创建一条完整的银行账号；任一字段为空则抛出（Req 3.9：一旦填写则三项必填）。
     *
     * @throws IllegalArgumentException 户名/开户银行名称/银行账号未同时填写
     */
    public static SupplierBankAccount of(String accountName, String bankName, String accountNumber) {
        SupplierBankAccount account = SupplierBankAccount.builder()
                .accountName(accountName)
                .bankName(bankName)
                .accountNumber(accountNumber)
                .build();
        if (!account.isComplete()) {
            throw new IllegalArgumentException("银行信息一旦填写，户名、开户银行名称、银行账号必须同时填写");
        }
        return account;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
