import type { BankAccountDto } from '../../types/dto/supplier.dto';

/**
 * 银行账号值对象（前端编辑态）。
 */
export type BankAccount = BankAccountDto;

/** 创建空白银行账号（用于「新增一组」） */
export function emptyBankAccount(): BankAccount {
  return { accountName: '', bankName: '', accountNumber: '' };
}

/** 判断银行账号三项是否全部为空（用于过滤未填写的空行） */
export function isBankAccountBlank(account: BankAccount): boolean {
  return (
    !account.accountName?.trim() &&
    !account.bankName?.trim() &&
    !account.accountNumber?.trim()
  );
}
