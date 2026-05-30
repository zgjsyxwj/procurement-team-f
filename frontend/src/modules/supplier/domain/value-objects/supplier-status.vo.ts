import type { SupplierStatus } from '../../types/vo/supplier-info.vo';

/**
 * 供应商状态值对象 —— 派生状态相关的前端业务判断。
 */

/** 入驻草稿阶段（信息保存即时生效，不进审核）：创建成功/待进入/待完善信息 */
export function isDraftStage(status: SupplierStatus): boolean {
  return status === 'CREATED' || status === 'PENDING_ENTRY' || status === 'PENDING_INFO';
}

/** 供应商端可编辑企业信息：待完善信息或合作中（待审核信息期间锁定） */
export function isSupplierEditable(status: SupplierStatus): boolean {
  return status === 'PENDING_INFO' || status === 'ACTIVE';
}

/** 是否可提交准入审核（仅待完善信息） */
export function canSubmitForReview(status: SupplierStatus): boolean {
  return status === 'PENDING_INFO';
}

/** 供应商端提交信息后是否走审核流程（合作中→待审核；草稿阶段→直接生效） */
export function submissionRequiresReview(status: SupplierStatus): boolean {
  return status === 'ACTIVE';
}

/** 是否合作中（可参与报价） */
export function isActive(status: SupplierStatus): boolean {
  return status === 'ACTIVE';
}
