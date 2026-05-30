import { ref } from 'vue';
import { defineStore } from 'pinia';
import type { SupplierDto, SupplierListQuery } from '../../types/dto/supplier.dto';
import * as supplierService from '../../infrastructure/services/supplier.service';
import * as changeService from '../../infrastructure/services/supplier-change.service';

/**
 * 供应商管理状态（Pinia）。
 * <p>
 * 维护：供应商端当前企业信息、采购端列表筛选状态、审核中心待办计数。
 */
export const useSupplierStore = defineStore('supplier', () => {
  // 供应商端：本企业信息
  const myProfile = ref<SupplierDto | null>(null);

  // 采购端：列表筛选条件（跨页保持）
  const listQuery = ref<SupplierListQuery>({ page: 0, size: 10 });

  // 审核中心：待办计数
  const pendingChangeCount = ref(0);

  async function loadMyProfile(): Promise<void> {
    myProfile.value = await supplierService.getMyProfile();
  }

  function clearMyProfile(): void {
    myProfile.value = null;
  }

  function setListQuery(query: SupplierListQuery): void {
    listQuery.value = query;
  }

  /** 刷新审核中心待办计数 */
  async function refreshPendingCount(): Promise<void> {
    try {
      const items = await changeService.getPendingChanges();
      pendingChangeCount.value = items.length;
    } catch {
      pendingChangeCount.value = 0;
    }
  }

  return {
    myProfile,
    listQuery,
    pendingChangeCount,
    loadMyProfile,
    clearMyProfile,
    setListQuery,
    refreshPendingCount,
  };
});
