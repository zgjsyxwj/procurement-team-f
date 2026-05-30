import type { SupplierDto } from '../../types/dto/supplier.dto';

/**
 * 供应商基本信息字段元数据（与后端 SupplierBasicInfoFields 注册表对齐）。
 * <p>
 * 用于：供应商端表单渲染、变更前后对比的字段显示名映射。
 * 银行等多值字段不在此（单独处理）。
 */

export type BasicFieldType = 'text' | 'textarea' | 'date' | 'number' | 'integer' | 'boolean';

export interface BasicInfoFieldMeta {
  key: keyof SupplierDto;
  label: string;
  type: BasicFieldType;
  required?: boolean;
}

export const BASIC_INFO_FIELDS: BasicInfoFieldMeta[] = [
  { key: 'name', label: '供应商名称', type: 'text', required: true },
  { key: 'unifiedSocialCreditCode', label: '统一社会信用代码', type: 'text' },
  { key: 'legalPerson', label: '公司法人', type: 'text' },
  { key: 'registeredDate', label: '注册时间', type: 'date' },
  { key: 'registeredCapital', label: '注册资金', type: 'number' },
  { key: 'address', label: '公司地址', type: 'text' },
  { key: 'generalTaxpayer', label: '一般纳税人', type: 'boolean' },
  { key: 'businessScope', label: '经营范围', type: 'textarea' },
  { key: 'enterpriseNature', label: '企业性质', type: 'text' },
  { key: 'salesMode', label: '销售模式', type: 'text' },
  { key: 'coverageArea', label: '覆盖区域', type: 'text' },
  { key: 'annualRevenue', label: '本年度营业额', type: 'number' },
  { key: 'employeeCount', label: '员工人数', type: 'integer' },
  { key: 'mainCustomers', label: '主力客户', type: 'text' },
];

/** 字段显示名映射（key -> label），供变更对比展示。 */
export const BASIC_INFO_FIELD_LABELS: Record<string, string> = BASIC_INFO_FIELDS.reduce(
  (map, field) => {
    map[field.key as string] = field.label;
    return map;
  },
  {} as Record<string, string>,
);

/** 银行字段显示名（变更对比时承载序列化文本，给一个友好显示名）。 */
export const BANK_FIELD_LABEL = '银行信息';
