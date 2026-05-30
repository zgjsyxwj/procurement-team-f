import type { SupplierDto } from '../../types/dto/supplier.dto';
import { isBankAccountBlank } from '../value-objects/bank-account.vo';

/**
 * 供应商实体（前端领域模型）。
 */
export type Supplier = SupplierDto;

/** 是否已填写银行信息（存在至少一组非空账号） */
export function hasBankInfo(supplier: Supplier): boolean {
  return (supplier.bankAccounts ?? []).some((account) => !isBankAccountBlank(account));
}
