import { apiClient } from '@/shared/http/api-client';
import type { ContactDto, SaveContactRequest } from '../../types/dto/contact.dto';

/**
 * 联系人接口（双端）。
 * <p>
 * 供应商端走 `/api/supplier/contacts**`；采购端走 `/api/suppliers/{id}/contacts**`。
 * 通过 supplierId 是否为 null 区分（null=供应商端本企业）。
 */

function base(supplierId: number | null): string {
  return supplierId == null ? '/api/supplier/contacts' : `/api/suppliers/${supplierId}/contacts`;
}

/** 联系人列表 */
export async function getContacts(supplierId: number | null): Promise<ContactDto[]> {
  const res = await apiClient.get<ContactDto[]>(base(supplierId));
  return res.data;
}

/** 新增联系人 */
export async function createContact(
  supplierId: number | null,
  data: SaveContactRequest,
): Promise<void> {
  await apiClient.post(base(supplierId), data);
}

/** 编辑联系人（即时生效） */
export async function updateContact(
  supplierId: number | null,
  contactId: number,
  data: SaveContactRequest,
): Promise<void> {
  await apiClient.put(`${base(supplierId)}/${contactId}`, data);
}

/** 删除联系人（不可删唯一主要联系人） */
export async function deleteContact(supplierId: number | null, contactId: number): Promise<void> {
  await apiClient.delete(`${base(supplierId)}/${contactId}`);
}

/** 设为主要联系人（自动取消原主要） */
export async function setPrimaryContact(
  supplierId: number | null,
  contactId: number,
): Promise<void> {
  await apiClient.patch(`${base(supplierId)}/${contactId}/primary`);
}

/** 向联系人发送门户邀请（仅采购端） */
export async function inviteContact(supplierId: number, contactId: number): Promise<void> {
  await apiClient.post(`/api/suppliers/${supplierId}/contacts/${contactId}/invite`);
}
