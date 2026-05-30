import * as contactService from '../infrastructure/services/contact.service';
import { extractErrorMessage, type UseCaseResult } from './extract-error';
import { validateContactForm, canDeleteContact } from '../domain/rules/contact-validation.rule';
import type { ContactDto, SaveContactRequest } from '../types/dto/contact.dto';

/**
 * 联系人管理用例（双端：supplierId=null 为供应商端本企业）。
 */

/** 联系人列表 */
export async function fetchContacts(supplierId: number | null): Promise<ContactDto[]> {
  return contactService.getContacts(supplierId);
}

/** 保存联系人（contactId 为空=新增，否则=编辑） */
export async function executeSaveContact(
  supplierId: number | null,
  contactId: number | null,
  data: SaveContactRequest,
): Promise<UseCaseResult> {
  const validationError = validateContactForm(data);
  if (validationError) {
    const firstError = validationError.name || validationError.phone || validationError.email;
    return { success: false, error: firstError || '表单验证失败' };
  }

  const payload: SaveContactRequest = {
    ...data,
    name: data.name.trim(),
    phone: data.phone.trim(),
    email: data.email.trim(),
  };

  try {
    if (contactId == null) {
      await contactService.createContact(supplierId, payload);
    } else {
      await contactService.updateContact(supplierId, contactId, payload);
    }
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '保存联系人失败，请稍后重试') };
  }
}

/** 设为主要联系人 */
export async function executeSetPrimary(
  supplierId: number | null,
  contactId: number,
): Promise<UseCaseResult> {
  try {
    await contactService.setPrimaryContact(supplierId, contactId);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '设置主要联系人失败，请稍后重试') };
  }
}

/** 删除联系人（前端拦截删唯一主要联系人，后端为权威） */
export async function executeDeleteContact(
  supplierId: number | null,
  target: ContactDto,
): Promise<UseCaseResult> {
  if (!canDeleteContact(target)) {
    return { success: false, error: '不可删除唯一的主要联系人，请先设置其他联系人为主要联系人' };
  }
  try {
    await contactService.deleteContact(supplierId, target.id);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '删除联系人失败，请稍后重试') };
  }
}

/** 向联系人发送门户邀请（仅采购端） */
export async function executeInviteContact(
  supplierId: number,
  contactId: number,
): Promise<UseCaseResult> {
  try {
    await contactService.inviteContact(supplierId, contactId);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '邀请发送失败，请稍后重试') };
  }
}
