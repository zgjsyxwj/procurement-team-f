import type { BankAccount } from '../value-objects/bank-account.vo';
import { isBankAccountBlank } from '../value-objects/bank-account.vo';

/**
 * 银行信息校验规则（Req 3.9）。
 * <p>
 * 银行信息整体非必填；但一旦某一组填写了任意一项，则户名/开户银行名称/银行账号三者必填。
 */

export interface BankAccountError {
  index: number;
  accountName?: string;
  bankName?: string;
  accountNumber?: string;
}

/**
 * 校验银行账号列表，返回每组的字段级错误（空数组表示全部通过）。
 * 全空的组视为「未填写」，跳过校验。
 */
export function validateBankAccounts(accounts: BankAccount[]): BankAccountError[] {
  const errors: BankAccountError[] = [];

  accounts.forEach((account, index) => {
    if (isBankAccountBlank(account)) {
      return;
    }

    const error: BankAccountError = { index };
    let hasError = false;

    if (!account.accountName?.trim()) {
      error.accountName = '请输入户名';
      hasError = true;
    }
    if (!account.bankName?.trim()) {
      error.bankName = '请输入开户银行名称';
      hasError = true;
    }
    if (!account.accountNumber?.trim()) {
      error.accountNumber = '请输入银行账号';
      hasError = true;
    }

    if (hasError) {
      errors.push(error);
    }
  });

  return errors;
}
