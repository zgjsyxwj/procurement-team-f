package com.cdp.ecosaas.procurement.supplier.interfaces.dto;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierBankAccount;

/**
 * 银行账号 DTO（户名/开户银行/账号，Req 3.9）。
 */
public record BankAccountDto(String accountName, String bankName, String accountNumber) {

    public static BankAccountDto from(SupplierBankAccount account) {
        return new BankAccountDto(account.getAccountName(), account.getBankName(), account.getAccountNumber());
    }
}
