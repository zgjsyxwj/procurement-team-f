import type { ContactDto, SaveContactRequest } from '../../types/dto/contact.dto';

/**
 * 联系人校验规则（Req 9.1-9.5）。
 */

/** 手机号格式（宽松：兼容国外，允许 +、空格、6-20 位数字） */
const PHONE_PATTERN = /^[+]?[\d\s-]{6,20}$/;
/** 邮箱格式 */
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export interface ContactFormError {
  name?: string;
  phone?: string;
  email?: string;
}

/**
 * 校验联系人表单（姓名/手机号/邮箱必填，格式校验）。
 */
export function validateContactForm(data: SaveContactRequest): ContactFormError | null {
  const errors: ContactFormError = {};

  if (!data.name?.trim()) {
    errors.name = '请输入姓名';
  }
  if (!data.phone?.trim()) {
    errors.phone = '请输入手机号';
  } else if (!PHONE_PATTERN.test(data.phone.trim())) {
    errors.phone = '请输入有效的手机号';
  }
  if (!data.email?.trim()) {
    errors.email = '请输入邮箱';
  } else if (!EMAIL_PATTERN.test(data.email.trim())) {
    errors.email = '请输入有效的邮箱地址';
  }

  return Object.keys(errors).length > 0 ? errors : null;
}

/**
 * 判断能否删除该联系人（Req 9.5）：不可删除唯一的主要联系人。
 * <p>
 * 主要联系人由部分唯一索引保证全局唯一；删除它会使企业无主要联系人，故禁止。
 * 需先将其他联系人设为主要，再删除原主要联系人。
 */
export function canDeleteContact(target: ContactDto): boolean {
  return !target.primary;
}
