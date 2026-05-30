import * as supplierService from '../infrastructure/services/supplier.service';
import type { SupplierDto } from '../types/dto/supplier.dto';

/**
 * 供应商端企业信息维护用例（读取本企业信息 + 基本信息校验）。
 */

/** 加载本企业信息 */
export async function fetchMyProfile(): Promise<SupplierDto> {
  return supplierService.getMyProfile();
}

export interface BasicInfoError {
  name?: string;
  registeredCapital?: string;
  annualRevenue?: string;
  employeeCount?: string;
}

/**
 * 校验企业基本信息（必填、注册资金正数）。
 * @param basicInfo 字段 key -> 字符串值
 */
export function validateBasicInfo(basicInfo: Record<string, string>): BasicInfoError | null {
  const errors: BasicInfoError = {};

  if (!basicInfo.name?.trim()) {
    errors.name = '请输入企业名称';
  }

  const capital = basicInfo.registeredCapital?.trim();
  if (capital && (!isNumeric(capital) || Number(capital) <= 0)) {
    errors.registeredCapital = '注册资金须为正数';
  }

  const revenue = basicInfo.annualRevenue?.trim();
  if (revenue && (!isNumeric(revenue) || Number(revenue) < 0)) {
    errors.annualRevenue = '本年度营业额须为非负数';
  }

  const employees = basicInfo.employeeCount?.trim();
  if (employees && (!Number.isInteger(Number(employees)) || Number(employees) < 0)) {
    errors.employeeCount = '员工人数须为非负整数';
  }

  return Object.keys(errors).length > 0 ? errors : null;
}

function isNumeric(value: string): boolean {
  return value !== '' && !Number.isNaN(Number(value));
}
