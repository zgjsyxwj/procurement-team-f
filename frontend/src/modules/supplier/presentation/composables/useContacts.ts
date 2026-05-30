import { ref } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  fetchContacts,
  executeSaveContact,
  executeSetPrimary,
  executeDeleteContact,
  executeInviteContact,
} from '../../application/manage-contacts.usecase';
import type { ContactDto, SaveContactRequest } from '../../types/dto/contact.dto';

/**
 * 联系人管理组合式函数（封装加载 + 增删改 + 设主 + 邀请的状态与交互）。
 *
 * @param supplierId 供应商 ID；null 表示供应商端本企业。
 */
export function useContacts(supplierId: number | null) {
  const contacts = ref<ContactDto[]>([]);
  const loading = ref(false);
  const saving = ref(false);

  async function load(): Promise<void> {
    loading.value = true;
    try {
      contacts.value = await fetchContacts(supplierId);
    } catch {
      message.error('加载联系人失败');
    } finally {
      loading.value = false;
    }
  }

  async function save(
    payload: { contactId: number | null; data: SaveContactRequest },
    onDone?: () => void,
  ): Promise<void> {
    saving.value = true;
    const result = await executeSaveContact(supplierId, payload.contactId, payload.data);
    saving.value = false;
    if (result.success) {
      message.success('保存成功');
      onDone?.();
      await load();
    } else {
      message.error(result.error || '保存失败');
    }
  }

  async function setPrimary(contact: ContactDto): Promise<void> {
    const result = await executeSetPrimary(supplierId, contact.id);
    if (result.success) {
      message.success('已设为主要联系人');
      await load();
    } else {
      message.error(result.error || '设置失败');
    }
  }

  function remove(contact: ContactDto): void {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除联系人 "${contact.name}" 吗？`,
      async onOk() {
        const result = await executeDeleteContact(supplierId, contact);
        if (result.success) {
          message.success('删除成功');
          await load();
        } else {
          message.error(result.error || '删除失败');
        }
      },
    });
  }

  async function invite(contact: ContactDto): Promise<void> {
    if (supplierId == null) return;
    const result = await executeInviteContact(supplierId, contact.id);
    if (result.success) {
      message.success('邀请已发送');
    } else {
      message.error(result.error || '邀请失败');
    }
  }

  return { contacts, loading, saving, load, save, setPrimary, remove, invite };
}
